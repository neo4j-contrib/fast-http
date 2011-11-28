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

import java.util.HashMap;
import java.util.Map;

public class InvocationRequest {

    private final Map<String, Object> ctxObjects = new HashMap<String, Object>();
    private PathVariables pathVariables;
    private Object deserializedContent;

    public InvocationRequest() {
    }
    
    
    public void putCtx(String key, Object obj) {
        ctxObjects.put(key, obj);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCtx(String key) {
        return (T)ctxObjects.get(key);
    }


    public PathVariables getPathVariables() {
        return pathVariables;
    }


    public void setPathVariables(PathVariables pathVariables) {
        this.pathVariables = pathVariables;
    }


    @SuppressWarnings("unchecked")
    public <T> T getDeserializedContent() {
        return (T)deserializedContent;
    }


    public void setDeserializedContent(Object deserializedContent) {
        this.deserializedContent = deserializedContent;
    }
}
