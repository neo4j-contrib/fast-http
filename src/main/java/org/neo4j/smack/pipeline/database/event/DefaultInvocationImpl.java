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
package org.neo4j.smack.pipeline.database.event;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.smack.pipeline.database.TransactionRegistry;
import org.neo4j.smack.routing.PathVariables;

/**
 * Note: There are lots of these instances, keep it as slim as possible to 
 * keep memory usage down.
 */
public class DefaultInvocationImpl implements Invocation {

    private PathVariables pathVariables;
    private Object content;
    
    private GraphDatabaseService database;
    private TransactionRegistry txRegistry;
    private long txId = -1l;
    private String path;

    @Override
    public PathVariables getPathVariables() {
        return pathVariables;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getContent() {
        return (T)content;
    }
    
    @Override
    public <T> T getContent(Class<T> type) {
        if (content==null) return null;
        if (type.isInstance(content)) return type.cast(content);
        throw new ClassCastException("Expected "+type+" found "+content.getClass());
    }
    
    @Override
    public GraphDatabaseService getDB() {
        return database;
    }
    
    @Override
    public TransactionRegistry getTxRegistry() {
        return txRegistry;
    }
    
    @Override
    public long getTxId() {
        return txId;
    }
    
    @Override
    public String getPath() {
        return path;
    }

    @Override
    public long getLongParameter(String name, long defaultValue)
    {
        return pathVariables.getLongParameter(name, defaultValue);
    }

    @Override
    public String getStringParameter(String name)
    {
        return pathVariables.getParameter(name);
    }
    
    @Override
    public String getStringParameter(String name, String defaultValue)
    {
        return pathVariables.getParameter(name, defaultValue);
    }

    protected void reset(String path, long txId, PathVariables pathVariables, Object content, GraphDatabaseService database, TransactionRegistry txRegistry)
    {   
        this.path = path;
        this.pathVariables = pathVariables;
        this.content = content;
        
        this.database = database;
        this.txRegistry = txRegistry;
        this.txId = txId;
    }
}
