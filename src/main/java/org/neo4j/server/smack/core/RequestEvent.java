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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.neo4j.server.smack.Endpoint;
import org.neo4j.server.smack.routing.InvocationVerb;
import org.neo4j.server.smack.routing.PathVariables;

import com.lmax.disruptor.EventFactory;

public class RequestEvent implements Fallible {
   
    public static EventFactory<RequestEvent> FACTORY = new EventFactory<RequestEvent>() {
        public RequestEvent newInstance() {
            return new RequestEvent();
        }
    };

    private InvocationVerb verb;
    
    private String path;
    
    private ChannelBuffer content;
    
    private PathVariables pathVariables;
    
    private Endpoint endpoint;
    
    private Object deserializedContent;
    
    private ChannelHandlerContext context;

    private boolean isPersistentConnection;

    private Throwable failure;

    public void setVerb(InvocationVerb verb) {
        this.verb = verb;
    }

    public InvocationVerb getVerb() {
        return verb;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    } 

    public void setContent(ChannelBuffer content) {
        this.content = content;
    }

    public void setIsPersistentConnection(boolean isPersistentConnection) {
        this.isPersistentConnection = isPersistentConnection;
    }

    public void setPathVariables(PathVariables pathVariables) {
        this.pathVariables = pathVariables;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public PathVariables getPathVariables() {
        return pathVariables;
    }

    public ChannelBuffer getContent() {
        return content;
    }

    public void setDeserializedContent(Object deserialized) {
        this.deserializedContent = deserialized;
    }

    public Object getDeserializedContent() {
        return deserializedContent;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    @Override
    public void setFailure(Throwable e) {
        this.failure = e;        
    }

    @Override
    public Throwable getFailure() {
        return failure;
    }

    @Override
    public boolean hasFailed() {
        return failure != null;
    }
}
