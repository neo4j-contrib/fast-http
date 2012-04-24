package org.neo4j.smack.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.neo4j.smack.MutableString;


public class TestHttpHeaderDecoder {

    @Test
    public void testDecodeSingleHeader() throws Exception
    {
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        
        buf.writeBytes("Content-Length: 0".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        
        HttpHeaderDecoder decoder = new HttpHeaderDecoder(new HashSet<HttpHeaderName>() {
            private static final long serialVersionUID = 1L;

            {
                add(HttpHeaderNames.CONTENT_LENGTH);
            }
        }, 4000);
        
        HttpHeaderContainer headers = new HttpHeaderContainer();
        
        decoder.decode(buf, headers);
        
        assertThat(headers.getHeader(HttpHeaderNames.CONTENT_LENGTH), is(new MutableString("0")));
    }

    @Test
    public void testDecodeMultipleHeaders() throws Exception
    {
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        
        buf.writeBytes("Content-Length: 0".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        buf.writeBytes("Accept : stuff".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        buf.writeBytes("Content-Length: 4".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        
        HttpHeaderDecoder decoder = new HttpHeaderDecoder(new HashSet<HttpHeaderName>() {
            private static final long serialVersionUID = 1L;

            {
                add(HttpHeaderNames.CONTENT_LENGTH);
                add(HttpHeaderNames.ACCEPT);
            }
        }, 4000);
        
        HttpHeaderContainer headers = new HttpHeaderContainer();
        
        decoder.decode(buf, headers);
        
        assertThat(headers.getHeaders(HttpHeaderNames.CONTENT_LENGTH).get(0), is(new MutableString("0")));
        assertThat(headers.getHeaders(HttpHeaderNames.CONTENT_LENGTH).get(1), is(new MutableString("4")));
        
        assertThat(headers.getHeader(HttpHeaderNames.ACCEPT), is(new MutableString("stuff")));
    }
    
    @Test
    public void testDecodeMultipleMultilineHeaders() throws Exception
    {
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        
        buf.writeBytes("Content-Length: this".getBytes("ASCII"));
        buf.writeBytes(" is a multiline string".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        buf.writeBytes("Accept : stuff".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        
        HttpHeaderDecoder decoder = new HttpHeaderDecoder(new HashSet<HttpHeaderName>() {
            private static final long serialVersionUID = 1L;

            {
                add(HttpHeaderNames.CONTENT_LENGTH);
                add(HttpHeaderNames.ACCEPT);
            }
        }, 4000);
        
        HttpHeaderContainer headers = new HttpHeaderContainer();
        
        decoder.decode(buf, headers);
        
        assertThat(headers.getHeaders(HttpHeaderNames.CONTENT_LENGTH).get(0), is(new MutableString("this is a multiline string")));
        
        assertThat(headers.getHeader(HttpHeaderNames.ACCEPT), is(new MutableString("stuff")));
    }
    
    @Test
    public void testDecodeHeadersOnlySeparatedByLinefeed() throws Exception
    {
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        
        buf.writeBytes("Accept : stuff".getBytes("ASCII"));
        buf.writeByte(HttpTokens.LF);
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        
        HttpHeaderDecoder decoder = new HttpHeaderDecoder(new HashSet<HttpHeaderName>() {
            private static final long serialVersionUID = 1L;

            {
                add(HttpHeaderNames.CONTENT_LENGTH);
                add(HttpHeaderNames.ACCEPT);
            }
        }, 4000);
        
        HttpHeaderContainer headers = new HttpHeaderContainer();
        
        decoder.decode(buf, headers);
        
        assertThat(headers.getHeader(HttpHeaderNames.ACCEPT), is(new MutableString("stuff")));
    }

}
