package org.neo4j.smack.test.util;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

/** 
 * Incomplete implementation of a HTTP client that does pipelining.
 * Used as a lab tool to see how we can maximize performance.
 */
public class PipelinedHttpClient {
    
    public static class HttpClientPipelineFactory implements ChannelPipelineFactory {

        private HttpResponseHandler responseHandler;

        public HttpClientPipelineFactory(HttpResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }
        
        public ChannelPipeline getPipeline() throws Exception {
            // Create a default pipeline implementation.
            ChannelPipeline pipeline = pipeline();

            pipeline.addLast("codec", new HttpClientCodec());

            // Uncomment the following line if you don't want to handle HttpChunks.
            pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));

            pipeline.addLast("handler", responseHandler);
            return pipeline;
        }

    } 
    
    public static class HttpResponseHandler extends SimpleChannelUpstreamHandler {
        public int responseCount = 0;

        public HttpResponse lastResponse;
        public Throwable lastException = null;
        
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                throws Exception {
            lastResponse = (HttpResponse) e.getMessage();
            responseCount++;
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            lastException = e.getCause();
        }
    }
    
    private Channel channel;
    private ClientBootstrap bootstrap;
    
    public HttpResponseHandler responseHandler = new HttpResponseHandler();

    public PipelinedHttpClient(String host, int port) {

        // Configure the client.
        bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpClientPipelineFactory(responseHandler));

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host,
                port));

        // Wait until the connection attempt succeeds or fails.
        channel = future.awaitUninterruptibly().getChannel();
        if (!future.isSuccess()) {
            bootstrap.releaseExternalResources();
            throw new RuntimeException(future.getCause());
        }
    }

    public ChannelFuture handle(HttpMethod method, URI uri, String payload) {

        if(responseHandler.lastException != null) {
            throw new RuntimeException(responseHandler.lastException);
        }
        
        String host = uri.getHost() == null ? "localhost" : uri.getHost();

        // Prepare the HTTP request.
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                method, uri.getPath());
        request.setHeader(HttpHeaders.Names.HOST, host);
        request.setHeader(HttpHeaders.Names.CONNECTION, "keep-alive");
        
        if(payload != null) {
            request.setContent(ChannelBuffers.copiedBuffer(payload, CharsetUtil.UTF_8));
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, request.getContent().readableBytes());
        } else {
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }
        
        // Send the HTTP request.
        return channel.write(request);
        
    }
    
    // Quick hack to wait for responses
    public void waitForXResponses(int count) throws InterruptedException {
        while(responseHandler.responseCount < count) {
            if(responseHandler.lastException != null) {
                responseHandler.lastException.printStackTrace();
                throw new RuntimeException(responseHandler.lastException);
            }
            Thread.sleep(3);
        }
    }

    public void close() {
        // Shut down executor threads to exit.
        bootstrap.releaseExternalResources();
    }


    
}
