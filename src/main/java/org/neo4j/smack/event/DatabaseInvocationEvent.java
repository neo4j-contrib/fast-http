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

    public boolean isPersistentConnection;

    public long id;

    private ChannelHandlerContext context;
    private boolean error;

    public void setFailed() {
        this.error = true;
    }

    @Override
    public boolean hasFailed() {
        return error;
    }

    public void setContext(ChannelHandlerContext outputChannel) {
        this.context = outputChannel;
        this.error = false;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public Result getResult() {
        return result;
    }
}
