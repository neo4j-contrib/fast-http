package org.neo4j.server.rest.service;

import org.neo4j.graphdb.Transaction;
import org.neo4j.server.database.Database;
import org.neo4j.server.smack.core.ResponseEvent;
import org.neo4j.server.transaction.TransactionRegistry;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;

public class DatabaseWorkHandler implements WorkHandler<DatabaseWork> {
    
    private Database database;
    private TransactionRegistry txs;
    private RingBuffer<ResponseEvent> output;
    
    private long currentTxId = -1l;
    
    public DatabaseWorkHandler(Database database, TransactionRegistry txs, RingBuffer<ResponseEvent> output) {
        this.database = database;
        this.output = output;
        this.txs = txs;
    }

    @Override
    public void onEvent(DatabaseWork work) throws Exception {
        if(work.txId != currentTxId)
        {
            txs.suspendCurrentTransaction();
            
            if(work.usesTxAPI)
            {
                currentTxId = work.txId;
                txs.associateWithCurrentThread(work.txId);
            } 
            else if(work.isTransactional)
            {   
                Transaction tx = database.graph.beginTx();
                try
                {
                    perform(work);
                    tx.success();  
                    return;
                } finally {
                    tx.finish();
                }
            }
        } 
       
        perform(work);
    }
    
    public void perform(DatabaseWork work) throws Exception {
        work.endpoint.invoke(work.request, work.response);
        long sequenceId = output.next();
        ResponseEvent ev = output.get(sequenceId);

        ev.setInvocationResponse(work.response);
        ev.setOutputChannel(work.request.getOutputChannel());
        output.publish(sequenceId);
    }

}
