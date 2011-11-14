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

import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.server.database.Database;
import org.neo4j.server.serialization.PropertyMapDeserializationStrategy;
import org.neo4j.server.smack.InvocationRequest;
import org.neo4j.server.smack.InvocationResponse;
import org.neo4j.server.smack.annotations.DeserializeWith;
import org.neo4j.server.smack.annotations.Parameters;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Map;

public class CoreService {

    private CoreServiceActions actions = new CoreServiceActions();

    @Path("/info")
    @GET
    public void databaseInfo(InvocationRequest req, InvocationResponse res) throws Exception {
        Database db = req.getCtx(ContextKeys.DATABASE);
        final AbstractGraphDatabase gdb = (AbstractGraphDatabase) db.getGraphDB();
        // TODO gdb.getConfig().getParams().toString()
        res.setOk();
    }
    //
    // NODES
    //
    
    @POST
    @Path("/node")
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @Parameters({"transactional:true"})
    public void createNode(InvocationRequest req, InvocationResponse res) throws Exception {
        Database db = req.getCtx(ContextKeys.DATABASE);
        Map<String, Object> properties = req.getDeserializedContent();
        System.out.println("properties = " + properties);
        Long id = actions.createNode(db, properties);
        
        res.setCreated("/node/" + id);
    }
    /* TODO
    @GET
    @Path("/node/{id}")
    // @SerializeWith(PropertyMapSerializationStrategy.class)
    public void readNode(InvocationRequest req, InvocationResponse res, Long id) throws Exception {
        Database db = req.getCtx(ContextKeys.DATABASE);
        Map<String, Object> properties = req.getDeserializedContent();
        System.out.println("properties = " + properties);
        Node node = actions.getNode(db,id);

        res.setCreated("/node/" + node.getId()); // todo props + rels
    }
    */

}
