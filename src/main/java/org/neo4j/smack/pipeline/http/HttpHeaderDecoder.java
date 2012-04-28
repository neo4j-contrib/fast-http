package org.neo4j.smack.pipeline.http;

import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.neo4j.smack.gcfree.MutableString;

public class HttpHeaderDecoder {

//    Content-length : 0
//    Some-random-header : a value
//      can continue on the next line
//      which is insane
//    Some-other-header: 12
    
    private final HttpHeaderName [] headersToCareAbout;
    private final int maxHeaderSize;
    private final MutableString rawHeaderName = new MutableString(32);
    private final MutableString value = new MutableString(128);
    
    private HttpHeaderName headerName;
    private int headerSize;
    
    public HttpHeaderDecoder(Set<HttpHeaderName> headersToCareAbout, int maxHeaderSize) {

        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException(
                    "maxHeaderSize must be a positive integer: " +
                    maxHeaderSize);
        }
        
        this.headersToCareAbout = headersToCareAbout.toArray(new HttpHeaderName [headersToCareAbout.size()]);
        this.maxHeaderSize = maxHeaderSize;
    }
    
    public void decode(ChannelBuffer buf, HttpHeaderContainer output) throws TooLongFrameException {
        int currentIndex,
            lineStopIndex;
        
        headerName = null;
        headerSize = 0;
            
        // Read one line
        currentIndex = buf.readerIndex();
        lineStopIndex = readLine(buf);
        
        // Parse loop
        if(lineStopIndex-currentIndex != 0) {
            do {
                char firstChar = (char)buf.getByte(currentIndex);
                if (headerName != null && (firstChar == ' ' || firstChar == '\t')) {
                    value.append(' ');
                    while(currentIndex++ < lineStopIndex) {
                        value.append((char)buf.getByte(currentIndex));
                    }
                } else {
                    if (headerName != null) {
                        output.addHeader(headerName, value);
                    }
                    
                    // Read header name
                    currentIndex = readHeader(buf, currentIndex, lineStopIndex);
                    
                    // Skip space after header
                    currentIndex = skipStuffBetweenNameAndValue(buf, currentIndex, lineStopIndex);
                    
                    // Read header value
                    currentIndex = readValue(buf, currentIndex, lineStopIndex);
                }

                currentIndex = buf.readerIndex();
                lineStopIndex = readLine(buf);
            } while (lineStopIndex-currentIndex != 0);

            // Add the last header.
            if (headerName != null) {
                output.addHeader(headerName, value);
            }
        }
    }
//            
//            // Parse line
//            for(;currentIndex<=lineStopIndex;currentIndex++) {
//            
//                nextByte = (char)buf.getByte(currentIndex);
//                
//                switchStatement:
//                switch(state){
//                case SKIP_INITIAL_WHITESPACE : 
//                    do {
//                        nextByte = (char)buf.readByte();
//                    } while(Character.isWhitespace(nextByte));
//                    
//                case READ_HEADER_NAME :
//                    rawHeaderName.reset();
//                    while(nextByte != ':' && !Character.isWhitespace(nextByte)) {
//                        rawHeaderName.append(nextByte);
//                        nextByte = (char)buf.readByte();
//                    }
//                    
//                    for(int i=0;i<headersToCareAbout.length;i++) 
//                    {
//                        if(headersToCareAbout[i].equalsString(rawHeaderName)) 
//                        {
//                            headerName = headersToCareAbout[i];
//                            state = State.SKIP_SPACE_AFTER_HEADER_NAME;
//                            break switchStatement;
//                        }
//                    }
//                    
//                    // This is a header we don't care about
//                    state = State.SKIP_UNTIL_NEXT_HEADER_STARTS;
//                    break switchStatement;
//                    
//                case SKIP_SPACE_AFTER_HEADER_NAME:
//                    while(nextByte != ':') {
//                        nextByte = (char)buf.readByte();
//                    }
//                    do {
//                        nextByte = (char)buf.readByte();
//                    } while(Character.isWhitespace(nextByte));
//                    
//                case READ_VALUE:
//                    value.reset();
//                    valueLoop:
//                    for(;;) {
//                        switch (nextByte) {
//                        case HttpTokens.CR:
//                            nextByte = (char) buf.readByte();
//                            headerSize ++;
//                            if (nextByte == HttpTokens.LF) {
//                                break valueLoop;
//                            }
//                            break;
//                        case HttpTokens.LF:
//                            break valueLoop;
//                        }
//                        
//                        value.append(nextByte);
//                        nextByte = (char)buf.readByte();
//                    }
//    
//                    output.addHeader(headerName, value);
//                    break switchStatement;
//                    
//                case SKIP_UNTIL_NEXT_HEADER_STARTS:
//                    break mainLoop;
//                }
//            }
//        }
    
    private int readValue(ChannelBuffer buf, int currentIndex, int lineStopIndex)
    {
        value.reset();
        char nextByte;
        
        while(currentIndex < lineStopIndex)  
        {
            nextByte = (char)buf.getByte(currentIndex++);
            value.append(nextByte);
        }
        
        return currentIndex;
    }

    // Hops past the ' : ' part of 'Content-length : 0'
    private int skipStuffBetweenNameAndValue(ChannelBuffer buf,
            int currentIndex, int lineStopIndex)
    {
        char nextByte = (char)buf.getByte(currentIndex);
        
        // Read up until ':'
        while(nextByte != ':' && currentIndex < lineStopIndex) 
        {
            nextByte = (char)buf.getByte(++currentIndex);
        }
        
        // Read until end of whitespace
        if(currentIndex < lineStopIndex) 
        {
            do {
                nextByte = (char)buf.getByte(++currentIndex);
            } while(Character.isWhitespace(nextByte) && currentIndex < lineStopIndex);
        }
        
        return currentIndex;
    }

    private int readHeader(ChannelBuffer buf, int currentIndex,
            int lineStopIndex)
    {
        rawHeaderName.reset();
        headerName = null;
        
        char nextByte = (char)buf.getByte(currentIndex);
        while(nextByte != ':' && !Character.isWhitespace(nextByte) && currentIndex < lineStopIndex) {
            rawHeaderName.append(nextByte);
            nextByte = (char)buf.getByte(++currentIndex);
        }
        
        for(int i=0;i<headersToCareAbout.length;i++) 
        {
            if(headersToCareAbout[i].equalsString(rawHeaderName)) 
            {
                headerName = headersToCareAbout[i];
                break;
            }
        }
        
        return currentIndex;
    }

    private int readLine(ChannelBuffer buf) throws TooLongFrameException
    {
        int skip = 1;
        char nextByte;
        lineLoop:
        for (;;) {
            nextByte = (char) buf.readByte();
            headerSize ++;

            switch (nextByte) 
            {
            case HttpTokens.CR:
                nextByte = (char) buf.readByte();
                headerSize ++;
                if (nextByte == HttpTokens.LF) {
                    skip = 2;
                    break lineLoop;
                }
                break;
            case HttpTokens.LF:
                skip = 1;
                break lineLoop;
            }

            // Abort decoding if the header part is too large.
            if (headerSize >= maxHeaderSize) {
                // TODO: Respond with Bad Request and discard the traffic
                //    or close the connection.
                //       No need to notify the upstream handlers - just log.
                //       If decoding a response, just throw an exception.
                throw new TooLongFrameException(
                        "HTTP header is larger than " +
                        maxHeaderSize + " bytes.");

            }
        }
        return buf.readerIndex() - skip; // -2 to not include CR LF
    }
    
}
