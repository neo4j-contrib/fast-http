package org.neo4j.smack.test.performance;

import java.net.URI;
import java.util.Date;

import org.neo4j.smack.Database;
import org.neo4j.smack.SmackServer;
import org.neo4j.smack.test.util.PerformanceRoutes;
import org.neo4j.smack.test.util.PipelinedHttpClient;
import org.neo4j.test.ImpermanentGraphDatabase;

/**
 * Meant to be used as a tool to maximize throughput in the
 * entire stack, including the network layer.
 *   
 * High scores
 *   smack,        pipelined,     2 channels     :  279 415.4628 req/second (2012-04-19, JH)
 *   smack,        pipelined,     4 channels     :  234 752.8052 req/second (2012-04-19, JH)
 *   smack,        pipelined,     single channel :  204 457.1662 req/second (2012-04-19, JH)
 *   jetty+jersey, pipelined,     single channel :   17 470.6057 req/second (2012-04-18, JH)
 *   smack,        non-pipelined, single channel :    8 443.8064 req/second (2012-04-18, JH)
 *   jetty+jersey, non-pipelined, single channel :      509.9959 req/second (2012-04-18, JH)
 *
 */
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
            //Thread.sleep(1000 * 15);
            
            int numRequests = 10000000;
            startServer();
            
            pipelineClient = new PipelinedHttpClient("localhost", 7473);
            
            // Simple HTTP calls
            
            Date start = new Date();
            //sendXRequests("http://localhost:7473" + PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION, numRequests);
            Date end = new Date();
            
            double totalSeconds =  (end.getTime() - start.getTime()) / 1000.0d;
            result.simpleHttpCalls = ((double)numRequests)/totalSeconds;
            
            // Pipelined calls
            pipelineClient.responseHandler.responseCount.set(0);
            
            System.out.println("Warming up..");
            sendXRequestsPipelined("http://localhost:7473" + PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION, 10000);
            pipelineClient.responseHandler.responseCount.set(0);
            
            System.out.println("Running test..");
            start = new Date();
            //sendXRequestsPipelined("http://localhost:7473" + PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION, numRequests);
            totalSeconds = sendXRequestsPipelinedMultiThreaded("http://localhost:7474/dummy/justreturn/200", numRequests, 2);
            end = new Date();
            
            //totalSeconds =  (end.getTime() - start.getTime()) / 1000.0d;
            System.out.println("Did " + numRequests + " http calls in " + totalSeconds + " seconds.");
            result.pipelinedCalls = ((double)numRequests)/totalSeconds;
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            stopServer();
        }
    }
    
    private double sendXRequestsPipelinedMultiThreaded(String uri,
            int numRequests, int numThreads) throws InterruptedException
    {
        Thread [] runnables = new Thread[numThreads];
        final int numRequestsPerThread = (int) numRequests / numThreads;
        for(int i=0;i<numThreads;i++) {
            runnables[i] = new Thread(new Runnable(){
                
                private PipelinedHttpClient client;

                {
                    client = new PipelinedHttpClient("localhost", 7473);
                }
                
                @Override
                public void run()
                {
                    for(int i=0;i<numRequestsPerThread;i+=10) {
                        //pipelineClient.handle(HttpMethod.GET, target, "");
                        client.sendRaw(10);
                    }
                    
                    try
                    {
                        client.waitForXResponses(numRequestsPerThread);
                    } catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        
        Date start = new Date();
        for(int i=0;i<numThreads;i++) {
            runnables[i].start();
        }
        
        for(int i=0;i<numThreads;i++) {
            runnables[i].join();
        }
        Date end = new Date();
        
        return (end.getTime() - start.getTime()) / 1000.0d;
    }

    private void sendXRequests(String uri, int numRequests) throws InterruptedException {
        URI target = URI.create(uri);
        for(int i=0;i<numRequests;i+=1) {
            //pipelineClient.handle(HttpMethod.GET, target, "");
            pipelineClient.sendRaw(1);
            pipelineClient.waitForXResponses(i);
        }
    }
    
    private void sendXRequestsPipelined(String uri, int numRequests) throws InterruptedException {
        URI target = URI.create(uri);
        for(int i=0;i<numRequests;i+=20) {
            //pipelineClient.handle(HttpMethod.GET, target, "");
            pipelineClient.sendRaw(20);
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
