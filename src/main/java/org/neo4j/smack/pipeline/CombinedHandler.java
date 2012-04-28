package org.neo4j.smack.pipeline;

import com.lmax.disruptor.WorkHandler;

public class CombinedHandler<E> implements WorkHandler<E> {

    private WorkHandler<E>[] handlers;

    public CombinedHandler(WorkHandler<E> ... handlers) {
        this.handlers = handlers;
    }
    
    @Override
    public void onEvent(E event) throws Exception
    {
        for(WorkHandler<E> handler : handlers) 
        {
            handler.onEvent(event);
        }
    }

}
