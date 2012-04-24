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
package org.neo4j.smack.event;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.neo4j.smack.serialization.SerializationFactory;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

/**
 * Note: There are lots of these instances, keep it as slim as possible to 
 * keep memory usage down.
 */
public class NettyChannelBackedOutput implements Output {

    private SerializationFactory serializationFactory;

    private boolean outputStarted;
    private Channel channel;
    private SerializationStrategy<Object> defaultSerializationStrategy;
    private boolean isPersistentConnection;

    public NettyChannelBackedOutput(SerializationFactory serializationFactory)
    {
        this.serializationFactory = serializationFactory;
    }

    @Override
    public void created()
    {
        send(CREATED, null, null);
    }

    @Override
    public void created(Object value)
    {
        send(CREATED, value, null);
    }

    @Override
    public void createdAt(String location)
    {
        send(CREATED, null, location);
    }

    @Override
    public void createdAt(String location, Object value)
    {
        send(CREATED, value, location);
    }

    @Override
    public void ok()
    {
        send(OK, null, null);
    }

    @Override
    public void ok(Object value)
    {
        send(OK, value, null);
    }

    @Override
    public void okNoContent()
    {
        send(NO_CONTENT, null, null);
    }

    @Override
    public void okAt(String location, Object value)
    {
        send(OK, value, location);
    }
    
    @Override
    public void notFound()
    {
        send(NOT_FOUND, null, null);
    }

    protected boolean started()
    {
        return outputStarted;
    }

    @SuppressWarnings("unchecked")
    protected void reset(Channel channel,
            SerializationStrategy<?> serializationStrategy,
            boolean isPersistentConnection)
    {
        this.channel = channel;
        this.defaultSerializationStrategy = (SerializationStrategy<Object>) serializationStrategy;
        this.isPersistentConnection = isPersistentConnection;

        outputStarted = false;
    }

    public void send(HttpResponseStatus status, Object data, String location)
    {
        send(status, data, location, defaultSerializationStrategy);
    }

    // TODO: Make this garbage free
    @SuppressWarnings("unchecked")
    public void send(
            HttpResponseStatus status,
            Object data,
            String location,
            @SuppressWarnings("rawtypes") SerializationStrategy serializationStrategy)
    {
        if (outputStarted)
        {
            throw new RuntimeException(
                    "Response has already been sent, can only send once per invocation.");
        }

        outputStarted = true;

        DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

        if (location != null)
        {
            response.addHeader(HttpHeaders.Names.LOCATION, location);
        }

        if (data == null)
        {
            response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);
        } else
        {
            if (serializationStrategy.isStreaming())
            {
                response.setChunked(true);
                throw new RuntimeException("IMPLEMENT ME!");
            } else
            {
                final DynamicChannelBuffer content = new DynamicChannelBuffer(
                        1000);
                Serializer serializer = serializationFactory
                        .getSerializer(content);
                serializationStrategy.serialize(data, serializer);
                response.setHeader(HttpHeaders.Names.CONTENT_TYPE, serializer
                        .getContentType().toString());
                response.setContent(content);
            }
        }

        ChannelFuture future = channel.write(response);

        if (!isPersistentConnection)
        {
            try
            {
                future.await().addListener(ChannelFutureListener.CLOSE);
            } catch (InterruptedException e)
            {
                // TODO: Create specific exception?
                throw new RuntimeException(
                        "Waiting for channel to be done to close it failed.", e);
            }
        }
    }
    
    //private void writeResponse(Serializer serializer)
}
