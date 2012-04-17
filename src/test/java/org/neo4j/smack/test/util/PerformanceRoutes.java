package org.neo4j.smack.test.util;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.NettyChannelBackedOutput;
import org.neo4j.smack.routing.RoutingDefinition;

public class PerformanceRoutes extends RoutingDefinition {
    
    public static final String NO_SERIALIZATION_AND_NO_DESERIALIZATION = "/noserialnodeserial";
    {
        addRoute("", new Object() {
            @GET
            @Path(NO_SERIALIZATION_AND_NO_DESERIALIZATION)
            public void noSerializationAndNoDeserialization(Invocation req, NettyChannelBackedOutput res) {
                res.ok();
            }
        });
    }
}
