package org.neo4j.smack.test.util;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * A http client for performance testing,
 * builds a byte array upon instantiation with
 * a fixed request to send, saving precious microseconds.
 */
public class FixedRequestClient extends PipelinedHttpClient {

    private ChannelBuffer buf;
    public FixedRequestClient(String host, int port, String path, int numRequestsPerBatch)
    {
        super(host, port);
        initRequestBuffer(numRequestsPerBatch, path, host);
    }
    
    private void initRequestBuffer(int numRequestsPerBatch, String path, String host)
    {
        buf = ChannelBuffers.dynamicBuffer(100);
        try {
            for(int i=0;i<numRequestsPerBatch;i++) {
                addRequestTo(buf, path, host);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void sendBatch() {
        channel.write(buf);
    }
    
    private void addRequestTo(ChannelBuffer buf, String path, String host) throws UnsupportedEncodingException
    {
        buf.writeBytes("GET".getBytes("ASCII"));
        buf.writeByte(SP);
        buf.writeBytes(path.getBytes("ASCII"));
        buf.writeByte(SP);
        buf.writeBytes(HttpVersion.HTTP_1_1.toString().getBytes("ASCII"));
        buf.writeByte(CR);
        buf.writeByte(LF);
        
        buf.writeBytes(HttpHeaders.Names.HOST.getBytes("ASCII"));
        buf.writeByte(COLON);
        buf.writeByte(SP);
        buf.writeBytes(host.getBytes("ASCII"));
        buf.writeByte(CR);
        buf.writeByte(LF);
        
        buf.writeBytes(HttpHeaders.Names.CONNECTION.getBytes("ASCII"));
        buf.writeByte(COLON);
        buf.writeByte(SP);
        buf.writeBytes("keep-alive".getBytes("ASCII"));
        buf.writeByte(CR);
        buf.writeByte(LF);
        
        buf.writeBytes(HttpHeaders.Names.CONTENT_LENGTH.getBytes("ASCII"));
        buf.writeByte(COLON);
        buf.writeByte(SP);
        buf.writeBytes("0".getBytes("ASCII"));
        buf.writeByte(CR);
        buf.writeByte(LF);

        buf.writeByte(CR);
        buf.writeByte(LF);
    }

}
