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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.neo4j.smack.serialization.SerializationStrategy;

import com.lmax.disruptor.EventFactory;

public class ResponseEvent implements Fallible {
    
    public static EventFactory<ResponseEvent> FACTORY = new EventFactory<ResponseEvent>() {
        public ResponseEvent newInstance() {
            return new ResponseEvent();
        }
    };
    
    private Result result;
    
    private HttpResponse httpResponse;
    
    private SerializationStrategy<?> serializationStrategy = SerializationStrategy.NO_OP;

    private ChannelHandlerContext context;

    private boolean isPersistentConnection;

    private long id;

    public void setInvocationResult(Result result) {
        this.result = result;
    }
    
    public Result getInvocationResult() {
        return result;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public void setSerializationStrategy(SerializationStrategy<?> serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    public SerializationStrategy<?> getSerializationStrategy() {
        return serializationStrategy;
    }

    public void setContext(ChannelHandlerContext outputChannel) {
        this.context = outputChannel;
    }

    public ChannelHandlerContext getContext() {
        return context;
    } 

    public void setFailed() {
        this.result.setFailed();
    }

    public boolean hasFailed() {
        if(result == null ) {
            System.out.println(result);
        }
        return result.hasFailed();
    }
    
    public void setIsPersistentConnection(boolean isPersistentConnection) {
        this.isPersistentConnection = isPersistentConnection;
    }

    public boolean getIsPersistentConnection() {
        return this.isPersistentConnection;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public long getId() {
        return id;
    }
}
