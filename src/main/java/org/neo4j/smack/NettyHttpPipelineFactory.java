/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.smack;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.neo4j.smack.event.RequestEvent;

import com.lmax.disruptor.RingBuffer;

public class NettyHttpPipelineFactory implements ChannelPipelineFactory {

    private RingBuffer<RequestEvent> workBuffer;
    private ChannelGroup openChannels;

    public NettyHttpPipelineFactory(RingBuffer<RequestEvent> workBuffer, ChannelGroup openChannels) {
        this.workBuffer = workBuffer;
        this.openChannels = openChannels;
    }
    
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();

        // Uncomment the following line if you want HTTPS
        // SSLEngine engine =
        // SecureChatSslContextFactory.getServerContext().createSSLEngine();
        // engine.setUseClientMode(false);
        // pipeline.addLast("ssl", new SslHandler(engine));

        pipeline.addLast("channeltracker",new NettyChannelTrackingHandler(openChannels));
        pipeline.addLast("decoder",       new HttpRequestDecoder());
        pipeline.addLast("aggregator",    new HttpChunkAggregator(65536));
        pipeline.addLast("encoder",       new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("handler",       new NettyHttpHandler(workBuffer));
        return pipeline;
    }
    
}
