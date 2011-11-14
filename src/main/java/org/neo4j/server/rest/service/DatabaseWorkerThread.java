package org.neo4j.server.rest.service;

import org.neo4j.server.database.Database;
import org.neo4j.server.smack.InvocationRequest;
import org.neo4j.server.smack.core.RequestEvent;
import org.neo4j.server.smack.core.ResponseEvent;
import org.neo4j.server.transaction.TransactionRegistry;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;

public class DatabaseWorkerThread extends BufferedWorkExecutor<DatabaseWork> {

    // Each worker thread keeps track of its own transactions
    private TransactionRegistry txs;
    private Database database;
    
    public DatabaseWorkerThread(Database database, TransactionRegistry txs,
            RingBuffer<ResponseEvent> output, ExceptionHandler exceptionHandler) {
        super(new DatabaseWorkHandler(database, txs, output), DatabaseWork.FACTORY, exceptionHandler);
        this.txs = txs;
        this.database = database;
    }
    
    public void addWork(RequestEvent event, Long txId, boolean transactional, boolean usesTxAPI) {
        
        long sequenceId = workBuffer.next();
        DatabaseWork work = workBuffer.get(sequenceId);
        
        work.txId = txId;
        work.isTransactional = transactional;
        work.endpoint = event.getEndpoint();
        work.usesTxAPI = usesTxAPI;
        
        InvocationRequest request = work.request;
        request.setPathVariables(event.getPathVariables());
        request.setDeserializedContent(event.getDeserializedContent());
        request.setOutputChannel(event.getOutputChannel());
        request.putCtx(ContextKeys.TX_REGISTRY, txs);
        request.putCtx(ContextKeys.TX_ID, txId);
        request.putCtx(ContextKeys.DATABASE, database);
        
        workBuffer.publish(sequenceId);
    }

}
