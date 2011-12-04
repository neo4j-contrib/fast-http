package org.neo4j.server.rest.service;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.neo4j.server.smack.Endpoint;
import org.neo4j.server.smack.Invocation;
import org.neo4j.server.smack.Result;
import org.neo4j.server.smack.core.Fallible;

import com.lmax.disruptor.EventFactory;

public class DatabaseWork implements Fallible {
    
    public static EventFactory<DatabaseWork> FACTORY = new EventFactory<DatabaseWork>() {
        public DatabaseWork newInstance() {
            return new DatabaseWork();
        }
    };
    
    public boolean isTransactional = false;
    
    public Invocation request = new Invocation();
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
