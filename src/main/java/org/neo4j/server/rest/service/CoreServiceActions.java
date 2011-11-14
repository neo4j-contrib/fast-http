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
package org.neo4j.server.rest.service;

import org.neo4j.graphdb.Node;
import org.neo4j.server.database.Database;

import java.util.Map;

public class CoreServiceActions {
    
    //
    // NODES
    //
    
    public long createNode(Database db, Map<String, Object> properties) {
        Node n = db.getGraphDB().createNode();
        if(properties != null) {
            for(String key : properties.keySet()) {
                n.setProperty(key, properties.get(key));
            }
        }
        return n.getId();
    }

    public Node getNode(Database db, Long id) {
        return db.getGraphDB().getNodeById(id);
    }
}
