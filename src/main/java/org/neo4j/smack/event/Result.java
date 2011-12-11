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
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

public class Result {
    
    private String location;
    
    private HttpResponseStatus status;
    
    private Object data;
    private ChannelHandlerContext context;
    private boolean failed =false;

    public void setCreated() {
        setStatus(CREATED);
    }
    public void setCreated(String location) {
        setCreated();
        this.setLocation(location);
    }
    
    public void setOk(){
        setStatus(OK);
    }

    public void setOk(Object value) {
        setOk();
        this.data = value;
    }

    public Object getData() {
        return data;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setContext(ChannelHandlerContext outputChannel) {
        this.context = outputChannel;
        this.failed = false;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setFailed() {
        this.failed =true;
    }
    public boolean hasFailed() {
        return failed;
    }
}
