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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.neo4j.smack.routing.Endpoint;
import org.neo4j.smack.routing.InvocationVerb;
import org.neo4j.smack.routing.PathVariables;
import org.neo4j.smack.routing.Routable;

import com.lmax.disruptor.EventFactory;

/**
 * Note: There are lots of these instances, keep it as slim as possible to 
 * keep memory usage down.
 */
public class RequestEvent implements Fallible, Routable, TransactionWork {
   
    public static EventFactory<RequestEvent> FACTORY = new EventFactory<RequestEvent>() {
        public RequestEvent newInstance() {
            return new RequestEvent();
        }
    };
    
    private final PathVariables pathVariables = new PathVariables();

    private InvocationVerb verb;
    
    private String path;
    
    private ChannelBuffer content;;
    
    private Endpoint endpoint;
    
    private Object deserializedContent;
    
    private Channel channel;
    
    private Throwable failure;

    private boolean isPersistentConnection;

    private Long connectionId;

    private long txId;

    private WorkTransactionMode txMode;
    
    

    public void setVerb(InvocationVerb verb) 
    {
        this.verb = verb;
    }

    public InvocationVerb getVerb() {
        return verb;
    }

    public String getPath() {
        return path;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public boolean getIsPersistentConnection() {
        return this.isPersistentConnection;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    // todo contention, distribute query params and Path Variables into different fields ? 
    public PathVariables getPathVariables() {
        return pathVariables;
    }

    public ChannelBuffer getInputBuffer() {
        return content;
    }

    public void setDeserializedContent(Object deserialized) {
        this.deserializedContent = deserialized;
    }

    public Object getDeserializedContent() {
        return deserializedContent;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void setFailed(Throwable ex) {
        this.failure = ex;
    }

    @Override
    public Throwable getFailureCause() {
        return this.failure;
    }

    @Override
    public boolean hasFailed() {
        return failure != null;
    }

    @Override
    public void setTransactionId(Long txId)
    {
        this.txId = txId;
    }

    @Override
    public long getTransactionId()
    {
        if(txId == -1l) 
        {
            txId = pathVariables.getLongParameter("tx_id", -1l);
        }
        return txId;
    }

    @Override
    public boolean isTransactional()
    {
        return endpoint != null ? endpoint.isTransactional() : false;
    }

    @Override
    public void setTransactionMode(WorkTransactionMode txMode)
    {
        this.txMode = txMode;
    }

    @Override
    public WorkTransactionMode getTransactionMode()
    {
        return txMode;
    }
    
    
    public void setChannel(Channel channel) 
    {
        this.channel = channel;
    }

    
    public long getConnectionId()
    {
        return connectionId;
    }

    public void reset(Long connectionId, InvocationVerb verb, String path,
            ChannelBuffer content, Channel channel, boolean keepAlive)
    {
        this.connectionId = connectionId;
        this.verb = verb;
        this.path = path;
        this.content = content;
        this.channel = channel;
        this.isPersistentConnection = keepAlive;
        
        this.endpoint = null;
        this.deserializedContent = null;
        this.txId = -1l;
        this.txMode = null;
        
        this.failure = null;

        this.pathVariables.reset();
    }

    public void reset(Long connectionId, Channel channel, Throwable cause)
    {
        reset(connectionId, null, null, null, channel, false);
        setFailed(cause);
    }
}
