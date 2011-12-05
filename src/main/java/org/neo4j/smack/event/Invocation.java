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

import org.neo4j.smack.Database;
import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.routing.PathVariables;

public class Invocation {

    private PathVariables pathVariables;
    private Object deserializedContent;
    
    private Database database;
    private TransactionRegistry txRegistry;
    private long txId = -1l;
    private String path;

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

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public void setTxRegistry(TransactionRegistry txRegistry) {
        this.txRegistry = txRegistry;
    }

    public TransactionRegistry getTxRegistry() {
        return txRegistry;
    }

    public void setTxId(long txId) {
        this.txId = txId;
    }

    public long getTxId() {
        return txId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
