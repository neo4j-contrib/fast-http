package org.neo4j.smack.test.performance;

import org.neo4j.smack.test.util.FixedRequestClient;
import org.neo4j.smack.test.util.PerformanceRoutes;

public class LoadGeneratingRunnable implements Runnable {
    
        
    private FixedRequestClient client;
    private long numRequestsToSend;
    private boolean logStuff;
    private int numWorkers;
    private int requestsPerBatch = 1000;

    public LoadGeneratingRunnable(boolean logStuff, long numRequestsToSend, int numWorkers){
        client = new FixedRequestClient("localhost", 7473, PerformanceRoutes.NO_SERIALIZATION_AND_NO_DESERIALIZATION_AND_NO_INTROSPECTION, requestsPerBatch );
        this.logStuff = logStuff;
        this.numRequestsToSend = numRequestsToSend;
        this.numWorkers = numWorkers;
    }
    
    @Override
    public void run()
    {
        for(int i=0;i<numRequestsToSend;i+=requestsPerBatch) 
        {
            client.sendBatch();
            try {
                // Used to help GC
                Thread.sleep(0,1);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(i % 100000 == 0 && i != 0) {
                if(logStuff) {
                    System.out.println("Sent: " + i * numWorkers + " requests");
                }
                client.waitForXResponses(i - 95000);
            }
        }
        
        client.waitForXResponses(numRequestsToSend);
    }

}
