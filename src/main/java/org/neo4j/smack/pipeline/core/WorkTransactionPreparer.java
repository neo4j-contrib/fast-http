package org.neo4j.smack.pipeline.core;

import org.neo4j.smack.pipeline.core.event.TransactionWork;
import org.neo4j.smack.pipeline.event.WorkTransactionMode;


public class WorkTransactionPreparer {

    private long txIds = 0l;

    public void prepare(TransactionWork event)
    {
        WorkTransactionMode txMode = WorkTransactionMode.NO_TRANSACTION;
        
        if(event.isTransactional()) 
        {
            txMode = WorkTransactionMode.OPEN_TRANSACTION;
        }

        long txId = event.getTransactionId();
        
        // Did the request come in with a tx id?
        if (txId == -1l) 
        {
            // Nope, generate one
            txId = txIds ++;
            
            // If this seemed like an open transaction, since client
            // did not provide a tx id, it is a single transaction.
            if(txMode == WorkTransactionMode.OPEN_TRANSACTION) {
                txMode = WorkTransactionMode.SINGLE_TRANSACTION;
            }
            
            event.setTransactionId(txId);
        }
        
        event.setTransactionMode(txMode);
    }

}
