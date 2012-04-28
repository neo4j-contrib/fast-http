package org.neo4j.smack.pipeline.http;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

// TODO: Write a garbage free encoder :)
public class HttpEncoder {

    public void encode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        
//        if (msg instanceof HttpMessage) {
//            HttpMessage m = (HttpMessage) msg;
//            boolean chunked = this.chunked = HttpCodecUtil.isTransferEncodingChunked(m);
//            ChannelBuffer header = ChannelBuffers.dynamicBuffer(
//                    channel.getConfig().getBufferFactory());
//            encodeInitialLine(header, m);
//            encodeHeaders(header, m);
//            header.writeByte(CR);
//            header.writeByte(LF);
//
//            ChannelBuffer content = m.getContent();
//            if (!content.readable()) {
//                return header; // no content
//            } else if (chunked) {
//                throw new IllegalArgumentException(
//                        "HttpMessage.content must be empty " +
//                        "if Transfer-Encoding is chunked.");
//            } else {
//                return wrappedBuffer(header, content);
//            }
//        }
    }
    
}
