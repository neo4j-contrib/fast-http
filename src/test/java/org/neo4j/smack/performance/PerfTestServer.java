package org.neo4j.smack.performance;

import org.neo4j.smack.Smack;
import org.neo4j.smack.test.util.PerformanceRoutes;
import org.neo4j.test.ImpermanentGraphDatabase;

public class PerfTestServer {

    private static Smack server;

    public static void main(String [] args) {
        server = new Smack("localhost", 7473, new ImpermanentGraphDatabase());
        server.addRoute("",new PerformanceRoutes());
        server.start();
        
        try
        {
            Thread.sleep(1000 * 60 * 60 * 24 * 7);
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        server.stop();
    }
    
}
