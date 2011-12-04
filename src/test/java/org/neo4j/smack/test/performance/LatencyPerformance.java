package org.neo4j.smack.test.performance;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.neo4j.smack.Database;
import org.neo4j.smack.SmackServer;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Result;
import org.neo4j.smack.routing.RoutingDefinition;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

public class LatencyPerformance {
    
    private static final String NO_SERIALIZATION_AND_NO_DESERIALIZATION = "/noserialnodeserial";
    
    public static class MockRoutes extends RoutingDefinition {{
        addRoute("", new Object() {
            @GET
            @Path(NO_SERIALIZATION_AND_NO_DESERIALIZATION)
            public void noSerializationAndNoDeserialization(Invocation req, Result res) {
                res.setOk();
            }
        });
    }}

    private SmackServer server;
    
    public static void main(String [] args) {
        LatencyPerformance latency = new LatencyPerformance();
        System.out.println("Running tests.. (this may take a while)");
        double avgLatency = latency.test();
        System.out.println("Average latency: " + avgLatency + "ms");
    }

    private double test() {
        try {
            
            int numRequests = 10000;
            
            startServer();
            
            Date start = new Date();
            sendXRequests("http://localhost:7474" + NO_SERIALIZATION_AND_NO_DESERIALIZATION, numRequests);
            Date end = new Date();
            
            long total = end.getTime() - start.getTime(); 
            return ((double)total)/numRequests;
            
        } finally {
            stopServer();
        }
    }
    
    private void sendXRequests(String uri, int numRequests) {
        Builder resource = Client.create().resource(uri).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
        for(int i=0;i<numRequests;i++) {
            ClientResponse response = resource.get(ClientResponse.class);
        }
    }
    
    private void startServer() {
        server = new SmackServer("localhost", 7474, new Database(new ImpermanentGraphDatabase()));
        server.addRoute("",new MockRoutes());
        server.start();
    }
    
    private void stopServer() {
        server.stop();
    }
}
