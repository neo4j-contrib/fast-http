package org.neo4j.smack.pipeline.database;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.smack.pipeline.RingBufferWorkPipeline;
import org.neo4j.smack.pipeline.core.event.CorePipelineEvent;
import org.neo4j.smack.pipeline.database.event.DatabaseWork;

import com.lmax.disruptor.ExceptionHandler;

public class DatabaseWorkPipeline extends RingBufferWorkPipeline<DatabaseWork>
{
    
    // Each database work pipeline keeps track of its own transactions
    private TransactionRegistry txs;
    private GraphDatabaseService database;

    private final ThreadTransactionManagement txManage;

    public DatabaseWorkPipeline(GraphDatabaseService database, TransactionRegistry txs, ThreadTransactionManagement txManage,
            ExceptionHandler exceptionHandler)
    {
        super("DatabaseWorkHandler", DatabaseWork.FACTORY, exceptionHandler, 512);
        this.txs = txs;
        this.txManage = txManage;
        this.database = database;

        addHandler(new DatabaseWorkPerformer());
    }

    public void addWork(CorePipelineEvent event)
    {

        long sequenceId = ringBuffer.next();
        DatabaseWork work = ringBuffer.get(sequenceId);

        if(!event.hasFailed()) {
            work.reset(
                    event.getEndpoint(), 
                    event.getChannel(),
                    event.getIsPersistentConnection(), 
                    event.getPath(), 
                    event.getTransactionId(),
                    event.getTransactionMode(),
                    event.getPathVariables(), 
                    event.getDeserializedContent(),
                    database, 
                    txs,
                    txManage);
        } else {
            work.reset(
                    event.getChannel(), 
                    event.getIsPersistentConnection(),  
                    event.getTransactionId(),
                    event.getTransactionMode(), 
                    database, 
                    txs,
                    txManage, 
                    event.getFailureCause());
        }

        ringBuffer.publish(sequenceId);
    }

}
