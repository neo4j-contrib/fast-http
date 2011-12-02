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

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.neo4j.graphdb.Node;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.server.annotations.Transactional;
import org.neo4j.server.database.Database;
import org.neo4j.server.serialization.NodeSerializationStrategy;
import org.neo4j.server.serialization.PropertyMapDeserializationStrategy;
import org.neo4j.server.smack.Invocation;
import org.neo4j.server.smack.Result;
import org.neo4j.server.smack.annotations.DeserializeWith;
import org.neo4j.server.smack.annotations.SerializeWith;

public class CoreService {

    private CoreServiceActions actions = new CoreServiceActions();

    @GET
    @Path("/info")
    public void databaseInfo(Invocation req, Result res) throws Exception {
        Database db = req.getDatabase();
        final AbstractGraphDatabase gdb = (AbstractGraphDatabase) db.getGraphDB();
        // TODO gdb.getConfig().getParams().toString()
        res.setOk();
    }
    
    //
    // NODES
    //
    
    @POST
    @Path("/node")
    @Transactional
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    public void createNode(Invocation req, Result res) throws Exception {
        Database db = req.getDatabase();
        Map<String, Object> properties = req.getDeserializedContent();
        res.setCreated("/node/" + actions.createNode(db, properties));
    }

    @GET
    @Path("/node/{id}")
    @SerializeWith(NodeSerializationStrategy.class)
    public void readNode(Invocation req, Result res) throws Exception {
        Database db = req.getDatabase();
        Long id = req.getPathVariables().getParamAsLong("id");
        Node node = actions.getNode(db,id);
        res.setOk(node);
    }
}
