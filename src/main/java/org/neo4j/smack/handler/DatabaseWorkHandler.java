package org.neo4j.smack.handler;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.smack.Database;
import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.event.DatabaseInvocationEvent;
import org.neo4j.smack.event.ResponseEvent;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;

public class DatabaseWorkHandler implements WorkHandler<DatabaseInvocationEvent> {
    
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
    public void onEvent(DatabaseInvocationEvent work) throws Exception {
        
        if(work.invocation.getTxId() != currentTxId)
        {
            txs.suspendCurrentTransaction();
            
            if(work.usesTxAPI)
            {
                txs.associateWithCurrentThread(work.invocation.getTxId());
                currentTxId = work.invocation.getTxId();
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

    public long perform(DatabaseInvocationEvent work) throws Exception 
    {
        work.endpoint.invoke(work.invocation, work.result);
        long sequenceId = output.next();
        ResponseEvent ev = output.get(sequenceId);
        
        ev.setFailure(null);
        ev.setSerializationStrategy(work.endpoint.getSerializationStrategy()); // todo
        ev.setInvocationResult(work.result);
        ev.setContext(work.getContext());
        
        return sequenceId;
    }

}
