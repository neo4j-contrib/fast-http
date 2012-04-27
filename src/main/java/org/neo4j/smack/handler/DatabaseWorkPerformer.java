package org.neo4j.smack.handler;

import org.neo4j.smack.event.DatabaseWork;

import com.lmax.disruptor.WorkHandler;

/**
 * Calls work.perform() from within 
 * an appropriate transactional context.
 */
public class DatabaseWorkPerformer implements WorkHandler<DatabaseWork> {
    
    @Override
    public void onEvent(DatabaseWork work) throws Exception 
    {
        try 
        {
            work.perform();
        } catch(Exception e) {
            // TODO: Logging
            e.printStackTrace();
        }
    }
}
