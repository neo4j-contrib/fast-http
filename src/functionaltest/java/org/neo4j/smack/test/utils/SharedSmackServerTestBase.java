package org.neo4j.smack.test.utils;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.server.smack.SmackServer;


public class SharedSmackServerTestBase {
    
    protected static final SmackServer server()
    {
        return server;
    }


    protected final void cleanDatabase()
    {
        ServerHelper.cleanTheDatabase( server );
    }

    private static SmackServer server;

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
}
