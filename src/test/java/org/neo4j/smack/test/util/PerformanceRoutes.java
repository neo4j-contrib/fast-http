package org.neo4j.smack.test.util;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.NettyChannelBackedOutput;
import org.neo4j.smack.event.Output;
import org.neo4j.smack.routing.Endpoint;
import org.neo4j.smack.routing.InvocationVerb;
import org.neo4j.smack.routing.RoutingDefinition;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.SerializationStrategy;

public class PerformanceRoutes extends RoutingDefinition {
    
    public static final String NO_SERIALIZATION_AND_NO_DESERIALIZATION = "/noserialnodeserial";
    public static final String NO_SERIALIZATION_AND_NO_DESERIALIZATION_AND_NO_INTROSPECTION = "/noserialnodeserialnointro";
    
    {
        addRoute("", new Object() {
            @GET
            @Path(NO_SERIALIZATION_AND_NO_DESERIALIZATION)
            public void noSerializationAndNoDeserialization(Invocation req, NettyChannelBackedOutput res) {
                res.ok();
            }
        });
        
        addRoute(NO_SERIALIZATION_AND_NO_DESERIALIZATION_AND_NO_INTROSPECTION, new Endpoint() {

            @Override
            public void invoke(Invocation invocation, Output result)
                    throws Exception
            {
                result.ok();
            }

            @Override
            public InvocationVerb getVerb()
            {
                return InvocationVerb.GET;
            }

            @Override
            public DeserializationStrategy<?> getDeserializationStrategy()
            {
                return DeserializationStrategy.NO_OP;
            }

            @Override
            public SerializationStrategy<?> getSerializationStrategy()
            {
                return SerializationStrategy.NO_OP;
            }

            @Override
            public boolean isTransactional()
            {
                return false;
            }
            
        });
    }
}
