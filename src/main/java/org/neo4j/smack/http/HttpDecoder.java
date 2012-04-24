package org.neo4j.smack.http;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.neo4j.smack.MutableString;
import org.neo4j.smack.WorkPublisher;
import org.neo4j.smack.http.HttpHeaderContainer.HttpHeaderValues;
import org.neo4j.smack.routing.InvocationVerb;

/**
 * Modified version of Nettys HttpDecoder. This decoder does not
 * create new HttpRequest objects, instead it builds up state for one
 * request, and then moves that state over to the Smack input pipeline via
 * a method call. That means less objects garbage collected, and
 * the potential for garbage freedom.
 * 
 * This is *very* much in dev right now, for instance chunking is epically
 * broken. Don't use for production.
 * 
 * TODO: Add chunked input support
 * TODO: Replace the readTrailingHeaders method with HttpHeaderDecoder to make it garbage free
 * TODO: Look into ReplayingHeaderDecoder, I think it buffers data and then does not reuse the buffers
 */
public class HttpDecoder extends ReplayingDecoder<HttpDecoder.State> {

    static enum State {
        SKIP_CONTROL_CHARS,
        READ_INITIAL,
        READ_HEADER,
        READ_VARIABLE_LENGTH_CONTENT,
        READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS,
        READ_FIXED_LENGTH_CONTENT,
        READ_FIXED_LENGTH_CONTENT_AS_CHUNKS,
        READ_CHUNK_SIZE,
        READ_CHUNKED_CONTENT,
        READ_CHUNKED_CONTENT_AS_CHUNKS,
        READ_CHUNK_DELIMITER,
        READ_CHUNK_FOOTER;
    }
    
    class DecodedHttpMessage {

        private boolean chunked;
        private Long contentLength;
        private HttpHeaderContainer headers = new HttpHeaderContainer();
        private HttpVersion protocolVersion;
        private InvocationVerb verb;
        private String path;

        HttpHeaderContainer getHeaderContainer() {
            return headers;
        }
        
        MutableString getHeader(HttpHeaderName name)
        {
            return headers.getHeader(name);
        }

        HttpHeaderValues getHeaders(HttpHeaderName name)
        {
            return headers.getHeaders(name);
        }

        HttpVersion getProtocolVersion()
        {
            return protocolVersion;
        }
        
        void removeHeader(HttpHeaderName name)
        {
            headers.removeHeader(name);
        }

        long getContentLength(long defaultValue)
        {
            return contentLength != null ? contentLength : defaultValue;
        }

        boolean isChunked()
        {
            return chunked;
        }

        void setChunked(boolean chunked)
        {
            this.chunked = chunked;
        }

        boolean isKeepAlive()
        {
            MutableString connection = getHeader(HttpHeaderNames.CONNECTION);
            if (CommonHeaderValues.CLOSE.equalsIgnoreCase(connection)) {
                return false;
            }

            if (protocolVersion.isKeepAliveDefault()) {
                return !CommonHeaderValues.CLOSE.equalsIgnoreCase(connection);
            } else {
                return CommonHeaderValues.KEEP_ALIVE.equalsIgnoreCase(connection);
            }
        }

        public InvocationVerb getVerb()
        {
            return verb;
        }

        public String getPath()
        {
            return path;
        }

        void reset(HttpVersion protocolVersion, InvocationVerb verb,
                String path)
        {
            this.protocolVersion = protocolVersion;
            this.verb = verb;
            this.path = path;
            this.headers.clear();
        }
    }
    
    protected final DecodedHttpMessage message = new DecodedHttpMessage();
    
    private final int maxInitialLineLength;
    private final int maxChunkSize;
    private final int maxHeaderSize;
    
    private final HttpHeaderDecoder headerDecoder;
    
    private final StringBuilder readHeaderStringBuilder = new StringBuilder(64);
    private final StringBuilder readLineStringBuilder = new StringBuilder(64);
    
    // Request state
    
    private long chunkSize;
    private int headerSize;

    private WorkPublisher workBuffer;

    private ChannelBuffer content;
    
    private boolean isDecodingRequest = true;

    public HttpDecoder(WorkPublisher workBuffer)
    {
        this(workBuffer, 4096, 8192, 8192, new HashSet<HttpHeaderName>(){{
            add(HttpHeaderNames.CONTENT_LENGTH);
            add(HttpHeaderNames.CONNECTION);
        }});
    }

    public HttpDecoder(WorkPublisher workBuffer, 
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, Set<HttpHeaderName> headersToCareAbout) {

        super(State.SKIP_CONTROL_CHARS, true);

        if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException(
                    "maxInitialLineLength must be a positive integer: " +
                    maxInitialLineLength);
        }
        if (maxChunkSize < 0) {
            throw new IllegalArgumentException(
                    "maxChunkSize must be a positive integer: " +
                    maxChunkSize);
        }
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxChunkSize = maxChunkSize;
        this.workBuffer = workBuffer;
        
        this.headerDecoder = new HttpHeaderDecoder(headersToCareAbout, maxHeaderSize);
        this.maxHeaderSize = maxHeaderSize;
    }

    /*
     * Work in progress
     */
    @Override
    @SuppressWarnings("fallthrough")
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
            ChannelBuffer buffer, State state) throws Exception
    {
        switch (state) {
        case SKIP_CONTROL_CHARS: {
            try {
                skipControlCharacters(buffer);
                checkpoint(State.READ_INITIAL);
            } finally {
                checkpoint();
            }
        }
        case READ_INITIAL: {
            String[] initialLine = splitInitialLine(readLine(buffer, maxInitialLineLength));
            if (initialLine.length < 3) {
                // Invalid initial line - ignore.
                checkpoint(State.SKIP_CONTROL_CHARS);
                return null;
            }

            intializeMessage(initialLine);
            checkpoint(State.READ_HEADER);
        }
        case READ_HEADER: {
            State nextState = readHeaders(buffer);
            checkpoint(nextState);
            if (nextState == State.READ_CHUNK_SIZE) {
                // Chunked encoding
                message.setChunked(true);
                // Generate DecodedHttpMessage first.  HttpChunks will follow.
                return message;
            } else if (nextState == State.SKIP_CONTROL_CHARS) {
                // No content is expected.
                // Remove the headers which are not supposed to be present not
                // to confuse subsequent handlers.
                message.removeHeader(HttpHeaderNames.TRANSFER_ENCODING);
                return message;
            } else {
                long contentLength = message.getContentLength(-1);
                if (contentLength == 0 || contentLength == -1 && isDecodingRequest ) {
                    content = ChannelBuffers.EMPTY_BUFFER;
                    return reset(ctx, channel);
                }

                switch (nextState) {
                case READ_FIXED_LENGTH_CONTENT:
                    if (contentLength > maxChunkSize || is100ContinueExpected(message)) {
                        // Generate DecodedHttpMessage first.  HttpChunks will follow.
                        checkpoint(State.READ_FIXED_LENGTH_CONTENT_AS_CHUNKS);
                        message.setChunked(true);
                        // chunkSize will be decreased as the READ_FIXED_LENGTH_CONTENT_AS_CHUNKS
                        // state reads data chunk by chunk.
                        chunkSize = message.getContentLength(-1);
                        return message;
                    }
                    break;
                case READ_VARIABLE_LENGTH_CONTENT:
                    if (buffer.readableBytes() > maxChunkSize || is100ContinueExpected(message)) {
                        // Generate DecodedHttpMessage first.  HttpChunks will follow.
                        checkpoint(State.READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS);
                        message.setChunked(true);
                        return message;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected state: " + nextState);
                }
            }
            // We return null here, this forces decode to be called again where we will decode the content
            return null;
        }
        case READ_VARIABLE_LENGTH_CONTENT: {
            if (content == null) {
                content = ChannelBuffers.dynamicBuffer(channel.getConfig().getBufferFactory());
            }
            //this will cause a replay error until the channel is closed where this will read what's left in the buffer
            content.writeBytes(buffer.readBytes(buffer.readableBytes()));
            return reset(ctx, channel);
        }
        case READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS: {
            // Keep reading data as a chunk until the end of connection is reached.
            int chunkSize = Math.min(maxChunkSize, buffer.readableBytes());
            HttpChunk chunk = new DefaultHttpChunk(buffer.readBytes(chunkSize));

            if (!buffer.readable()) {
                // Reached to the end of the connection.
                reset(ctx, channel);
                if (!chunk.isLast()) {
                    // Append the last chunk.
                    return new Object[] { chunk, HttpChunk.LAST_CHUNK };
                }
            }
            return chunk;
        }
        case READ_FIXED_LENGTH_CONTENT: {
            //we have a content-length so we just read the correct number of bytes
            readFixedLengthContent(buffer);
            return reset(ctx, channel);
        }
        case READ_FIXED_LENGTH_CONTENT_AS_CHUNKS: {
            long chunkSize = this.chunkSize;
            HttpChunk chunk;
            if (chunkSize > maxChunkSize) {
                chunk = new DefaultHttpChunk(buffer.readBytes(maxChunkSize));
                chunkSize -= maxChunkSize;
            } else {
                assert chunkSize <= Integer.MAX_VALUE;
                chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
                chunkSize = 0;
            }
            this.chunkSize = chunkSize;

            if (chunkSize == 0) {
                // Read all content.
                reset(ctx, channel);
                if (!chunk.isLast()) {
                    // Append the last chunk.
                    return new Object[] { chunk, HttpChunk.LAST_CHUNK };
                }
            }
            return chunk;
        }
        /**
         * everything else after this point takes care of reading chunked content. basically, read chunk size,
         * read chunk, read and ignore the CRLF and repeat until 0
         */
        case READ_CHUNK_SIZE: {
            String line = readLine(buffer, maxInitialLineLength);
            int chunkSize = getChunkSize(line);
            this.chunkSize = chunkSize;
            if (chunkSize == 0) {
                checkpoint(State.READ_CHUNK_FOOTER);
                return null;
            } else if (chunkSize > maxChunkSize) {
                // A chunk is too large. Split them into multiple chunks again.
                checkpoint(State.READ_CHUNKED_CONTENT_AS_CHUNKS);
            } else {
                checkpoint(State.READ_CHUNKED_CONTENT);
            }
        }
        case READ_CHUNKED_CONTENT: {
            assert chunkSize <= Integer.MAX_VALUE;
            HttpChunk chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
            checkpoint(State.READ_CHUNK_DELIMITER);
            return chunk;
        }
        case READ_CHUNKED_CONTENT_AS_CHUNKS: {
            long chunkSize = this.chunkSize;
            HttpChunk chunk;
            if (chunkSize > maxChunkSize) {
                chunk = new DefaultHttpChunk(buffer.readBytes(maxChunkSize));
                chunkSize -= maxChunkSize;
            } else {
                assert chunkSize <= Integer.MAX_VALUE;
                chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
                chunkSize = 0;
            }
            this.chunkSize = chunkSize;

            if (chunkSize == 0) {
                // Read all content.
                checkpoint(State.READ_CHUNK_DELIMITER);
            }

            if (!chunk.isLast()) {
                return chunk;
            }
        }
        case READ_CHUNK_DELIMITER: {
            for (;;) {
                byte next = buffer.readByte();
                if (next == HttpTokens.CR) {
                    if (buffer.readByte() == HttpTokens.LF) {
                        checkpoint(State.READ_CHUNK_SIZE);
                        return null;
                    }
                } else if (next == HttpTokens.LF) {
                    checkpoint(State.READ_CHUNK_SIZE);
                    return null;
                }
            }
        }
        case READ_CHUNK_FOOTER: {
            HttpChunkTrailer trailer = readTrailingHeaders(buffer);
            if (maxChunkSize == 0) {
                // Chunked encoding disabled.
                return reset(ctx, channel);
            } else {
                reset(ctx, channel);
                // The last chunk, which is empty
                return trailer;
            }
        }
        default: {
            throw new Error("Shouldn't reach here.");
        }

        }
    }

    private boolean is100ContinueExpected(DecodedHttpMessage message)
    {
        // It works only on HTTP/1.1 or later.
        if (message.getProtocolVersion().compareTo(HttpVersion.HTTP_1_1) < 0) {
            return false;
        }

        // In most cases, there will be one or zero 'Expect' header.
        MutableString value = message.getHeader(HttpHeaderNames.EXPECT);
        if (value == null) {
            return false;
        }
        if (CommonHeaderValues.CONTINUE.equalsIgnoreCase(value)) {
            return true;
        }

        // Multiple 'Expect' headers.  Search through them.
        HttpHeaderValues values = message.getHeaders(HttpHeaderNames.EXPECT);
        MutableString current = values.get(0);
        for( int i=0, l=values.size() ; i<l ; current=values.get(++i)) {
            if (CommonHeaderValues.CONTINUE.equalsIgnoreCase(current)) {
                return true;
            }
        }
        return false;
    }

    private void intializeMessage(String[] initialLine)
    {
        message.reset(HttpVersion.valueOf(initialLine[2]), InvocationVerb.valueOf(initialLine[0]), initialLine[1]);
    }

    protected boolean isContentAlwaysEmpty(DecodedHttpMessage msg) {
//        if (msg instanceof HttpResponse) {
//            HttpResponse res = (HttpResponse) msg;
//            int code = res.getStatus().getCode();
//            if (code < 200) {
//                return true;
//            }
//            switch (code) {
//            case 204: case 205: case 304:
//                return true;
//            }
//        }
        return false;
    }

    private Object reset(ChannelHandlerContext ctx, Channel channel) {
        
        Long connectionId = (Long)ctx.getAttachment();
        
        workBuffer.addWork(connectionId, message.getVerb(), message.getPath(), content, channel, message.isKeepAlive());

        checkpoint(State.SKIP_CONTROL_CHARS);
        return null;
    }

    private void skipControlCharacters(ChannelBuffer buffer) {
        for (;;) {
            char c = (char) buffer.readUnsignedByte();
            if (!Character.isISOControl(c) &&
                !Character.isWhitespace(c)) {
                buffer.readerIndex(buffer.readerIndex() - 1);
                break;
            }
        }
    }

    private void readFixedLengthContent(ChannelBuffer buffer) {
        long length = message.getContentLength(-1);
        assert length <= Integer.MAX_VALUE;

        if (content == null) {
            content = buffer.readBytes((int) length);
        } else {
            content.writeBytes(buffer.readBytes((int) length));
        }
    }

    protected State readHeaders(ChannelBuffer buffer) throws TooLongFrameException {
        headerDecoder.decode(buffer, message.getHeaderContainer());

        State nextState;

        if (isContentAlwaysEmpty(message)) {
            nextState = State.SKIP_CONTROL_CHARS;
        } else if (message.isChunked()) {
            // DecodedHttpMessage.isChunked() returns true when either:
            // 1) DecodedHttpMessage.setChunked(true) was called or
            // 2) 'Transfer-Encoding' is 'chunked'.
            // Because this decoder did not call DecodedHttpMessage.setChunked(true)
            // yet, DecodedHttpMessage.isChunked() should return true only when
            // 'Transfer-Encoding' is 'chunked'.
            nextState = State.READ_CHUNK_SIZE;
        } else if (message.getContentLength(-1) >= 0) {
            nextState = State.READ_FIXED_LENGTH_CONTENT;
        } else {
            nextState = State.READ_VARIABLE_LENGTH_CONTENT;
        }
        return nextState;
    }

    // TODO: Make garbage free
    private HttpChunkTrailer readTrailingHeaders(ChannelBuffer buffer) throws TooLongFrameException {
        headerSize = 0;
        String line = readHeader(buffer);
        String lastHeader = null;
        if (line.length() != 0) {
            HttpChunkTrailer trailer = new DefaultHttpChunkTrailer();
            do {
                char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    List<String> current = trailer.getHeaders(lastHeader);
                    if (current.size() != 0) {
                        int lastPos = current.size() - 1;
                        String newString = current.get(lastPos) + line.trim();
                        current.set(lastPos, newString);
                    } else {
                        // Content-Length, Transfer-Encoding, or Trailer
                    }
                } else {
                    String[] header = splitHeader(line);
                    String name = header[0];
                    if (!name.equalsIgnoreCase(HttpHeaders.Names.CONTENT_LENGTH) &&
                        !name.equalsIgnoreCase(HttpHeaders.Names.TRANSFER_ENCODING) &&
                        !name.equalsIgnoreCase(HttpHeaders.Names.TRAILER)) {
                        trailer.addHeader(name, header[1]);
                    }
                    lastHeader = name;
                }

                line = readHeader(buffer);
            } while (line.length() != 0);

            return trailer;
        }

        return HttpChunk.LAST_CHUNK;
    }

    private String readHeader(ChannelBuffer buffer) throws TooLongFrameException {
        readHeaderStringBuilder.setLength(0);
        int headerSize = this.headerSize;

        loop:
        for (;;) {
            char nextByte = (char) buffer.readByte();
            headerSize ++;

            switch (nextByte) {
            case HttpTokens.CR:
                nextByte = (char) buffer.readByte();
                headerSize ++;
                if (nextByte == HttpTokens.LF) {
                    break loop;
                }
                break;
            case HttpTokens.LF:
                break loop;
            }

            // Abort decoding if the header part is too large.
            if (headerSize >= maxHeaderSize ) {
                // TODO: Respond with Bad Request and discard the traffic
                //    or close the connection.
                //       No need to notify the upstream handlers - just log.
                //       If decoding a response, just throw an exception.
                throw new TooLongFrameException(
                        "HTTP header is larger than " +
                        maxHeaderSize + " bytes.");

            }

            readHeaderStringBuilder.append(nextByte);
        }

        this.headerSize = headerSize;
        return readHeaderStringBuilder.toString();
    }

    private int getChunkSize(String hex) {
        hex = hex.trim();
        for (int i = 0; i < hex.length(); i ++) {
            char c = hex.charAt(i);
            if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                hex = hex.substring(0, i);
                break;
            }
        }

        return Integer.parseInt(hex, 16);
    }

    // TODO: Make this garbage free (map bytes directly to appropriate enums etc. rather
    // than creating a string and then parsing that).
    private String readLine(ChannelBuffer buffer, int maxLineLength) throws TooLongFrameException {
        readLineStringBuilder.setLength(0);
        int lineLength = 0;
        while (true) {
            byte nextByte = buffer.readByte();
            if (nextByte == HttpTokens.CR) {
                nextByte = buffer.readByte();
                if (nextByte == HttpTokens.LF) {
                    return readLineStringBuilder.toString();
                }
            }
            else if (nextByte == HttpTokens.LF) {
                return readLineStringBuilder.toString();
            }
            else {
                if (lineLength >= maxLineLength) {
                    // TODO: Respond with Bad Request and discard the traffic
                    //    or close the connection.
                    //       No need to notify the upstream handlers - just log.
                    //       If decoding a response, just throw an exception.
                    throw new TooLongFrameException(
                            "An HTTP line is larger than " + maxLineLength +
                            " bytes.");
                }
                lineLength ++;
                readLineStringBuilder.append((char) nextByte);
            }
        }
    }

    // TODO: Make garbage free
    private String[] splitInitialLine(String sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);

        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);

        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[] {
                sb.substring(aStart, aEnd),
                sb.substring(bStart, bEnd),
                cStart < cEnd? sb.substring(cStart, cEnd) : "" };
    }

    private String[] splitHeader(String sb) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;

        nameStart = findNonWhitespace(sb, 0);
        for (nameEnd = nameStart; nameEnd < length; nameEnd ++) {
            char ch = sb.charAt(nameEnd);
            if (ch == ':' || Character.isWhitespace(ch)) {
                break;
            }
        }

        for (colonEnd = nameEnd; colonEnd < length; colonEnd ++) {
            if (sb.charAt(colonEnd) == ':') {
                colonEnd ++;
                break;
            }
        }

        valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            return new String[] {
                    sb.substring(nameStart, nameEnd),
                    ""
            };
        }

        valueEnd = findEndOfString(sb);
        return new String[] {
                sb.substring(nameStart, nameEnd),
                sb.substring(valueStart, valueEnd)
        };
    }

    private int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findEndOfString(String sb) {
        int result;
        for (result = sb.length(); result > 0; result --) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }

}
