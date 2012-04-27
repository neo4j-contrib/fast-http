package org.neo4j.smack.test.util;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.smack.Neo4jServer;


public class SharedSmackServerTestBase {

    private static Neo4jServer server;
    
    protected static final Neo4jServer server()
    {
        return server;
    }

    @BeforeClass
    public static void allocateServer() throws IOException
    {
        server = ServerHolder.allocate();
    }
    
    @AfterClass
    public static final void releaseServer()
    {
        try
        {
            ServerHolder.release( server );
        }
        finally
        {
            server = null;
        }
    }

    protected final void cleanDatabase()
    {
        ServerHelper.cleanTheDatabase( server );
    }
}
