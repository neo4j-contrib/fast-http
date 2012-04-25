package org.neo4j.smack.performance;

import org.neo4j.smack.Database;
import org.neo4j.smack.SmackServer;
import org.neo4j.smack.test.util.PerformanceRoutes;
import org.neo4j.test.ImpermanentGraphDatabase;

public class PerfTestServer {

    private static SmackServer server;

    public static void main(String [] args) {
        server = new SmackServer("localhost", 7473, new Database(new ImpermanentGraphDatabase()));
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
