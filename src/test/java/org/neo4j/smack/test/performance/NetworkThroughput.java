package org.neo4j.smack.test.performance;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.neo4j.smack.Database;
import org.neo4j.smack.SmackServer;
import org.neo4j.smack.test.util.PerformanceRoutes;
import org.neo4j.smack.test.util.PipelinedHttpClient;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

public class NetworkThroughput {
    
    private SmackServer server;
    private PipelinedHttpClient pipelineClient;
    
    /**
     * Gives throughput numbers over the network.
     * Each value denotes transactions executed per second,
     * against a server end point that does not do 
     * any database operations. 
     */
    public static final class NetworkThroughputResult {
        
        /**
         * Simple http calls are calls that wait for a 
         * response before the next request is sent.
         */
        public double simpleHttpCalls;
        
        /**
         * Number of pipelined http calls per second.
         */
        public double pipelinedCalls;
    }
    
    public static void main(String [] args) {
        NetworkThroughput throughput = new NetworkThroughput();
        System.out.println("Running network throughput tests.. (this may take a while)");
        NetworkThroughputResult result = throughput.test();
        System.out.println("Average throughput over network with simple HTTP calls: " + result.simpleHttpCalls + " requests/second");
        System.out.println("Average throughput over network with pipelined HTTP calls: " + result.pipelinedCalls + " requests/second");
    }

    private NetworkThroughputResult test() {
        NetworkThroughputResult result = new NetworkThroughputResult();
        try {
            
            int numRequests = 1000000;
            startServer();
            
            // Simple HTTP calls
            
            Date start = new Date();
            //sendXRequests("http://localhost:7473" + PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION, numRequests);
            Date end = new Date();
            
            double totalSeconds =  (end.getTime() - start.getTime()) / 1000.0d;
            result.simpleHttpCalls = ((double)numRequests)/totalSeconds;
            
            // Pipelined calls
            
            pipelineClient = new PipelinedHttpClient("localhost", 7473);
            
            start = new Date();
            sendXRequestsPipelined("http://localhost:7473" + PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION, numRequests);
            end = new Date();
            
            totalSeconds =  (end.getTime() - start.getTime()) / 1000.0d;
            result.pipelinedCalls = ((double)numRequests)/totalSeconds;
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
    
    private void sendXRequestsPipelined(String uri, int numRequests) throws InterruptedException {
        URI target = URI.create(uri);
        for(int i=0;i<numRequests;i++) {
            pipelineClient.handle(HttpMethod.GET, target, "");
        }
        
        pipelineClient.waitForXResponses(numRequests);
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
