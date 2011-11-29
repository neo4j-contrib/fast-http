package org.neo4j.server.rest.service;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;
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
        if(work.request.getTxId() != currentTxId)
        {
            txs.suspendCurrentTransaction();
            
            if(work.usesTxAPI)
            {
                txs.associateWithCurrentThread(work.request.getTxId());
                currentTxId = work.request.getTxId();
            } 
            else if(work.isTransactional)
            {   
                Transaction tx = database.graph.beginTx();
                long sequenceId = -1;
                try
                {
                    sequenceId = perform(work);
                    tx.success();  
                    return;
                } finally {
                    dumpDb(database.graph);
                    tx.finish();
                    publishResults(sequenceId);
                }
            }
        } 
       
        publishResults(perform(work));
    }

    private void publishResults(long sequenceId) {
        if (sequenceId>-1) {
            output.publish(sequenceId);
        }
    }

    private void dumpDb(final AbstractGraphDatabase gds) {
        for (Node node : gds.getAllNodes()) {
            System.out.println("node = " + node);
            for (String prop : node.getPropertyKeys()) {
                System.out.println(prop + ": "+node.getProperty(prop));
            }
        }
    }

    public long perform(DatabaseWork work) throws Exception {
        work.endpoint.invoke(work.request, work.result);
        long sequenceId = output.next();
        ResponseEvent ev = output.get(sequenceId);
        ev.setSerializationStrategy(work.endpoint.getSerializationStrategy()); // todo
        ev.setInvocationResult(work.result);
        return sequenceId;
    }

}
