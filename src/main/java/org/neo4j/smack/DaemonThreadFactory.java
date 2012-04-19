package org.neo4j.smack;

import java.util.concurrent.ThreadFactory;

/**
* @author mh
* @since 14.11.11
*/
public class DaemonThreadFactory implements ThreadFactory {
    
    private String baseName;
    private int threadNo = 0;

    public DaemonThreadFactory(String threadBaseName) {
        this.baseName = threadBaseName;
    }
    
    @Override
    public Thread newThread(Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setName(baseName + "-" + threadNo++);
        thread.setDaemon(true);
        return thread;
    }
}
