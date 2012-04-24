package org.neo4j.smack.test.performance;

import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.neo4j.smack.Database;
import org.neo4j.smack.SmackServer;
import org.neo4j.smack.test.util.FixedRequestClient;
import org.neo4j.smack.test.util.PerformanceRoutes;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * This is meant as a source of feedback for experimenting
 * with improving network latency.
 * 
 * High scores:
 *   smack        :      88.229 µs / req
 *   jetty+jersey :   1 971.900 µs / req 
 * 
 * Suggested things to try:
 *   - Look into adjusting TCP packet ACK rate from Java
 *   - Go through full call path, ensure it is garbage free
 *   - Look into optimizing the request router
 */
public class NetworkLatency {

    private SmackServer server;
    private FixedRequestClient pipelineClient;
    
    public static void main(String [] args) {
        NetworkLatency latency = new NetworkLatency();
        System.out.println("Running over-the-network request latency tests.. (this may take a while)");
        double avgLatency = latency.test();
        System.out.println("Average over-the-network request latency: " + avgLatency + "ms");
    }

    private double test() {
        try {
            
            int numRequests = 500000;
            
            //startServer();
            
            pipelineClient = new FixedRequestClient("localhost", 7473, PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION_AND_NO_INTROSPECTION, 1);
            
            Date start = new Date();
            sendXRequests("http://localhost:7473" + PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION_AND_NO_INTROSPECTION, numRequests);
            //sendXRequestsPipelined(numRequests);
            Date end = new Date();
            
            long total = end.getTime() - start.getTime(); 
            return ((double)total)/numRequests;
            
        } catch (Throwable e)
        {
            e.printStackTrace();
            return 0d;
        } finally {
            //stopServer();
        }
    }
    
    private void sendXRequests(String uri, int numRequests) {
        Builder resource = Client.create().resource(uri).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
        for(int i=0;i<numRequests;i++) {
            ClientResponse response = resource.get(ClientResponse.class);
        }
    }
    
    private void sendXRequestsPipelined(int numRequests) throws InterruptedException {
        for(int i=0;i<numRequests;i++) {
            pipelineClient.sendBatch();
            pipelineClient.waitForXResponses(i);
        }
    }
    
    private void startServer() {
        server = new SmackServer("localhost", 7473, new Database(new ImpermanentGraphDatabase()));
        server.addRoute("",new PerformanceRoutes());
        server.start();
    }
    
    private void stopServer() {
        server.stop();
    }
}
