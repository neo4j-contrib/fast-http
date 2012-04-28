package org.neo4j.smack.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.neo4j.smack.pipeline.database.event.Invocation;
import org.neo4j.smack.pipeline.database.event.Output;

public class DataOperationsDiscoveryService {

    @GET
    @Path("")
    public void getServiceDescription(Invocation invocation, Output result) 
    {
        result.ok();
    }
    
}
