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
package org.neo4j.server.smack.core;

import com.lmax.disruptor.EventFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.neo4j.server.smack.Result;
import org.neo4j.server.smack.serialization.SerializationStrategy;

public class ResponseEvent {
    
    public static EventFactory<ResponseEvent> FACTORY = new EventFactory<ResponseEvent>() {
        public ResponseEvent newInstance() {
            return new ResponseEvent();
        }
    };
    
    private Result result;
    private HttpResponse httpResponse;
    private SerializationStrategy<?> serializationStrategy = SerializationStrategy.NO_OP;

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

    public ChannelHandlerContext getContext() {
        return result.getContext(); // todo
    }
}
