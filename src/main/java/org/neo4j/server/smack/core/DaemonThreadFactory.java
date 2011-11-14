package org.neo4j.server.smack.core;

import java.util.concurrent.ThreadFactory;

/**
* @author mh
* @since 14.11.11
*/
public class DaemonThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    }
}
