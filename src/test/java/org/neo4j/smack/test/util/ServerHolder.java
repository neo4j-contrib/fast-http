package org.neo4j.smack.test.util;

import java.io.IOException;

import org.neo4j.smack.SmackServer;

public class ServerHolder extends Thread
{
    private static AssertionError allocation;
    private static SmackServer server;

    static synchronized SmackServer allocate() throws IOException
    {
        if ( allocation != null ) throw allocation;
        if ( server == null ) server = startServer();
        allocation = new AssertionError( "The server was allocated from here but not released properly" );
        return server;
    }

    static synchronized void release( SmackServer server )
    {
        if ( server == null ) return;
        if ( server != ServerHolder.server )
            throw new AssertionError( "trying to release a server not allocated from here" );
        if ( allocation == null ) throw new AssertionError( "releasing the server although it is not allocated" );
        allocation = null;
    }

    static synchronized void ensureNotRunning()
    {
        if ( allocation != null ) throw allocation;
        shutdown();
    }

    private static SmackServer startServer() throws IOException
    {
        SmackServer server = ServerHelper.createServer();
        server.start();
        return server;
    }

    private static synchronized void shutdown()
    {
        allocation = null;
        try
        {
            if ( server != null ) server.stop();
        }
        finally
        {
            server = null;
        }
    }

    @Override
    public void run()
    {
        shutdown();
    }

    static
    {
        Runtime.getRuntime().addShutdownHook( new ServerHolder() );
    }

    private ServerHolder()
    {
        super( ServerHolder.class.getName() );
    }

}
