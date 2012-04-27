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
package org.neo4j.smack.http;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.neo4j.smack.WorkPublisher;

public class NettyHttpHandler extends SimpleChannelHandler {

    private HttpDecoder httpDecoder;
    
    private AtomicLong connectionId;

    private WorkPublisher workBuffer;

    public NettyHttpHandler(WorkPublisher workBuffer, AtomicLong connectionIdGenerator) {
        this.workBuffer = workBuffer;
        this.httpDecoder = new HttpDecoder(workBuffer);
        this.connectionId = connectionIdGenerator;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        httpDecoder.messageReceived(ctx, e);
    }
    
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        ctx.setAttachment(connectionId.incrementAndGet());
    }

    // TODO: I think this catches both upstream and downstream
    // exceptions. Only upstream exceptions should get added to
    // the work buffer like this, down stream exceptions need
    // to be handled differently, otherwise we will append
    // an error for a requst that failed back to the beginning of
    // the list of jobs.
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        
        Long connectionId = (Long)ctx.getAttachment();
        workBuffer.addFailure(connectionId, ch, cause);
    }
    
}
