package org.neo4j.smack.event;

import org.jboss.netty.channel.Channel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.smack.ThreadTransactionManagement;
import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.handler.ExceptionOutputWriter;
import org.neo4j.smack.routing.Endpoint;
import org.neo4j.smack.routing.PathVariables;
import org.neo4j.smack.serialization.SerializationFactory;

import com.lmax.disruptor.EventFactory;

/**
 * Represents work that has been prepared and is ready to be performed
 * by the system by a single database thread, usually within some specific 
 * transaction and always by a specific database worker thread.
 * 
 * Note: There are lots of these instances, keep this as slim as possible to 
 * keep memory usage down.
 */
public class DatabaseWork implements Fallible {

    public static EventFactory<DatabaseWork> FACTORY = new EventFactory<DatabaseWork>() {

        // TODO: This shouldn't be here
        SerializationFactory serializationFactory = new SerializationFactory();

        public DatabaseWork newInstance()
        {
            return new DatabaseWork(serializationFactory);
        }
    };
    
    private static final ExceptionOutputWriter exceptionOutputWriter = new ExceptionOutputWriter();

    private Endpoint endpoint;

    private WorkTransactionMode txMode;

    private Channel channel;

    private final DefaultInvocationImpl invocation;

    private final NettyChannelBackedOutput output;

    private Throwable failure;

    private ThreadTransactionManagement txManage;

    public DatabaseWork(SerializationFactory serializationFactory)
    {
        this.invocation = new DefaultInvocationImpl();
        this.output = new NettyChannelBackedOutput(serializationFactory);
    }

    public void perform() throws Exception
    {
        try
        {
            if(!hasFailed()) {
                txManage.beforeWork(txMode, invocation.getTxId());
                endpoint.invoke(invocation, output);
                txManage.afterWork(txMode, invocation.getTxId());
                output.flush();
            } else {
                throw failure;
            }
        } catch (Throwable e)
        {
            txManage.onWorkFailure(txMode, invocation.getTxId());
            if (output.started())
            {
                channel.close();
            } else
            {
                exceptionOutputWriter.write(output, e);
                output.flush();
            }
        }
    }

    @Override
    public void setFailed(Throwable ex) {
        this.failure = ex;
    }

    @Override
    public Throwable getFailureCause() {
        return this.failure;
    }

    @Override
    public boolean hasFailed() {
        return failure != null;
    }

    public WorkTransactionMode getTransactionMode()
    {
        return txMode;
    }

    public Invocation getInvocation()
    {
        return invocation;
    }
    
    @Override
    public String toString() 
    {
        return "DatabaseWork[" + endpoint + ", txMode:" + txMode + ", failure:" + failure + "]";
    }

    public void reset(Channel channel, boolean isPersistentConnection, long txId, WorkTransactionMode txMode,
            GraphDatabaseService database, TransactionRegistry txs, ThreadTransactionManagement txManage, Throwable failureCause)
    {
        reset(null, channel, isPersistentConnection, null, txId, txMode, null, null, database, txs, txManage);
        setFailed(failureCause);
    }
    
    public void reset(Endpoint endpoint, Channel outputChannel,
            boolean isPersistentConnection, String path,
            long txId, WorkTransactionMode txMode, PathVariables pathVariables, Object content,
            GraphDatabaseService database, TransactionRegistry txRegistry, ThreadTransactionManagement txManage)
    {
        this.channel = outputChannel;
        this.endpoint = endpoint;

        this.txMode = txMode == null ? WorkTransactionMode.NO_TRANSACTION : txMode;
        this.failure = null;
        this.txManage = txManage;

        invocation.reset(path, txId, pathVariables, content, database, txRegistry);
        output.reset(outputChannel, endpoint != null ? endpoint.getSerializationStrategy() : null, isPersistentConnection);
    }
}
