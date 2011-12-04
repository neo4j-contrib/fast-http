package org.neo4j.smack.event;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.neo4j.smack.routing.Endpoint;

import com.lmax.disruptor.EventFactory;

public class DatabaseInvocationEvent implements Fallible {
    
    public static EventFactory<DatabaseInvocationEvent> FACTORY = new EventFactory<DatabaseInvocationEvent>() {
        public DatabaseInvocationEvent newInstance() {
            return new DatabaseInvocationEvent();
        }
    };
    
    public boolean isTransactional = false;
    
    public Invocation invocation = new Invocation();
    public Result result = new Result();

    public Endpoint endpoint;

    /**
     * True if this unit of work is expected to be performed within
     * an already existing and ongoing transaction.
     */
    public boolean usesTxAPI;

    private Throwable failure;

    private ChannelHandlerContext context;

    @Override
    public void setFailure(Throwable e) {
        this.failure = e;
    }

    @Override
    public Throwable getFailure() {
        return failure;
    }

    @Override
    public boolean hasFailed() {
        return failure != null;
    }

    public void setContext(ChannelHandlerContext outputChannel) {
        this.context = outputChannel;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }
}
