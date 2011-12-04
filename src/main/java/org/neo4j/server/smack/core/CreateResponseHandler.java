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

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.neo4j.server.smack.Result;
import org.neo4j.server.smack.routing.ResourceNotFoundException;

import com.lmax.disruptor.WorkHandler;

public class CreateResponseHandler implements WorkHandler<ResponseEvent> {

    private Map<Class<? extends Throwable>, HttpResponseStatus> exceptionToStatusMap = new HashMap<Class<? extends Throwable>, HttpResponseStatus>();
    
    public void onEvent(final ResponseEvent event) throws Exception 
    {    
        HttpResponse response;
        
        if(!event.hasFailed()) 
        {
            final Result result = event.getInvocationResult();
            response = new DefaultHttpResponse(HTTP_1_1, result.getStatus());
            if (result.getLocation() != null) 
            {
                response.addHeader(HttpHeaders.Names.LOCATION, result.getLocation());
            }
        } 
        else 
        {
            response = createFailureResponse(event);
        }
        
        event.setHttpResponse(response);
    }
    
    private HttpResponse createFailureResponse(ResponseEvent event) 
    {
        if(exceptionToStatusMap.containsKey(event.getFailure().getClass())) 
        {
            return new DefaultHttpResponse(HTTP_1_1, exceptionToStatusMap.get(event.getFailure().getClass()));
        } 
        else 
        {
            return new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private void mapException(Class<? extends Throwable> ex, HttpResponseStatus status) 
    {
        exceptionToStatusMap.put(ex, status);
    }
    
    {
        mapException(ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND);
    }
    
    
}
