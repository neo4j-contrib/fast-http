package org.neo4j.smack.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Test;
import org.neo4j.smack.WorkInputGate;
import org.neo4j.smack.routing.InvocationVerb;

public class TestHttpDecoder {

    class Request 
    {
        public Request(Long connectionId, InvocationVerb verb, String path, ChannelBuffer content, Channel channel, boolean keepAlive) 
        {
            this.connectionId = connectionId;
            this.verb = verb;
            this.path = path;
            this.content = content;
            this.channel = channel;
            this.keepAlive = keepAlive;        
        }
        
        public Long connectionId;
        public InvocationVerb verb;
        public String path;
        public ChannelBuffer content;
        public Channel channel;
        public boolean keepAlive;
    }
    
    class RequestMatcher 
    {
        public RequestMatcher(Long connectionId, InvocationVerb verb, String path) 
        {
            this(connectionId, verb, path, null, null, true);
        }
        
        public RequestMatcher(Long connectionId, InvocationVerb verb, String path, ChannelBuffer content, Channel channel, boolean keepAlive)
        {
            this.connectionId = connectionId;
            this.verb = verb;
            this.path = path;
            this.content = content;
            this.channel = channel;
            this.keepAlive = keepAlive;
        }
        
        public boolean matches(Request work) 
        {
            if(work.connectionId == connectionId && work.verb == verb && work.path.equals(path) && work.keepAlive == keepAlive) {
                return true;
            }
            return false;
        }
        
        public Long connectionId;
        public InvocationVerb verb;
        public String path;
        public ChannelBuffer content;
        public Channel channel;
        public boolean keepAlive;
    }
    
    class DummyInputGate implements WorkInputGate 
    {
        
        ArrayList<Request> requests = new ArrayList<Request>();

        @Override
        public void addWork(Long connectionId, InvocationVerb verb,
                String path, ChannelBuffer content, Channel channel,
                boolean keepAlive)
        {
            Request work = new Request(connectionId,verb,path,content,channel,keepAlive);
            requests.add(work);
        }
        
    }
    
    @Test
    public void shouldDecodeSimpleMessage() throws Exception 
    {   
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        
        buf.writeBytes("GET / HTTP/1.1".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        
        buf.writeBytes("Content-Length: 0".getBytes("ASCII"));
        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);

        buf.writeByte(HttpTokens.CR);
        buf.writeByte(HttpTokens.LF);
        
        testDecoding(buf, new RequestMatcher(0l, InvocationVerb.GET, "/"));
    }

    private void testDecoding(ChannelBuffer buf, RequestMatcher ... requestMatchers) throws Exception
    { 
        DummyInputGate inputGate = new DummyInputGate();
        
        HttpDecoder decoder = new HttpDecoder(inputGate);
        
        MessageEvent msg = mock(MessageEvent.class);
        when(msg.getMessage()).thenReturn(buf);
        
        ChannelConfig channelConfig = mock(ChannelConfig.class);
        when(channelConfig.getBufferFactory()).thenReturn(new HeapChannelBufferFactory());
        
        Channel channel = mock(Channel.class);
        when(channel.getConfig()).thenReturn(channelConfig);
        
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.getChannel()).thenReturn(channel);
        
        // Decode
        
        decoder.messageReceived(ctx, msg);
        
        // Check result
        
        assertThat("Should yield "+requestMatchers.length+" input requests", inputGate.requests.size(), is(requestMatchers.length));
    }
}
