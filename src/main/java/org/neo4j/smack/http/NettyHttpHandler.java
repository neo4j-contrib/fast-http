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

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.neo4j.smack.WorkInputGate;
import org.neo4j.smack.event.RequestEvent;

public class NettyHttpHandler extends SimpleChannelHandler {

    private HttpDecoder httpDecoder;
    
    private AtomicLong connectionId;

    public NettyHttpHandler(WorkInputGate workBuffer, AtomicLong connectionIdGenerator) {
        this.httpDecoder = new HttpDecoder(workBuffer);
        this.connectionId = connectionIdGenerator;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
//        HttpRequest httpRequest = (HttpRequest) e.getMessage();
//        Long connectionId = (Long)ctx.getAttachment();
//        InvocationVerb verb = InvocationVerb.valueOf(httpRequest.getMethod().getName().toUpperCase());
//        
//        workBuffer.addWork(connectionId, verb, httpRequest.getUri(), httpRequest.getContent(), ctx.getChannel(), isKeepAlive(httpRequest));
        httpDecoder.messageReceived(ctx, e);
    }

    // TODO: This should go in router
    private void addParamsAndPath(HttpRequest httpRequest, RequestEvent event) {
        final String uri = httpRequest.getUri();
        if (uri.contains("?")) {
            final QueryStringDecoder decoder = new QueryStringDecoder(uri);
            //event.getPathVariables().add(decoder.getParameters());
            //event.setPath(decoder.getPath());
        } else {
           //event.setPath(uri);
        }
    }

    // TODO: Create a failure RequestEvent from this, output
    // should not be done from here, it needs to be done from the
    // database worker thread assigned to this connection, because
    // otherwise responses may be sent out of order to the client.
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        ctx.setAttachment(connectionId.incrementAndGet());
        System.out.println("Assigned connection: " + connectionId.get());
    }
    
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response)
                .addListener(ChannelFutureListener.CLOSE);
    }
    
}
