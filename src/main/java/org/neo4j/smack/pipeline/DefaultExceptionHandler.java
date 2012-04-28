package org.neo4j.smack.pipeline;

import org.neo4j.smack.pipeline.event.Fallible;

import com.lmax.disruptor.ExceptionHandler;

public class DefaultExceptionHandler implements ExceptionHandler {

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event)
    {
        ex.printStackTrace();
        if(event instanceof Fallible) {
            ((Fallible)event).setFailed(ex);
        }
    }

    @Override
    public void handleOnStartException(Throwable ex)
    {
        ex.printStackTrace();
    }

    @Override
    public void handleOnShutdownException(Throwable ex)
    {
        ex.printStackTrace();
    }

}
