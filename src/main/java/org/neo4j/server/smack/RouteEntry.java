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
package org.neo4j.server.smack;

import com.sun.jersey.server.impl.uri.PathPattern;

public class RouteEntry {

    protected PathPattern pattern;
    
    protected Endpoint getEndpoint;
    protected Endpoint putEndpoint;
    protected Endpoint postEndpoint;
    protected Endpoint deleteEndpoint;
    protected Endpoint headEndpoint;
    
    public void setEndpoint(InvocationVerb verb, Endpoint endpoint) {
        System.out.println(this);
        switch(verb) {
        case GET:
            getEndpoint = endpoint;
            break;
        case PUT:
            putEndpoint = endpoint;
            break;
        case POST:
            postEndpoint = endpoint;
            break;
        case DELETE:
            deleteEndpoint = endpoint;
            break;
        case HEAD:
            headEndpoint = endpoint;
            break;
        }
    }
    
    public Endpoint getEndpoint(InvocationVerb verb) {
        switch(verb) {
        case GET:
            return getEndpoint;
        case PUT:
            return putEndpoint;
        case POST:
            return postEndpoint;
        case DELETE:
            return deleteEndpoint;
        case HEAD:
            return headEndpoint;
        }
        return null;
    }
    
    public String toString() {
        return "Route ["+pattern+"] {GET:"+getEndpoint+", PUT:"+putEndpoint+", POST:"+postEndpoint+", DELETE:"+deleteEndpoint+", HEAD:"+headEndpoint+"}";
    }
}
