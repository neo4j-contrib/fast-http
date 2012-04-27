package org.neo4j.smack.handler;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.event.DatabaseWork;

import com.lmax.disruptor.WorkHandler;

/**
 * Calls work.perform() from within 
 * an appropriate transactional context.
 */
public class DatabaseWorkPerformer implements WorkHandler<DatabaseWork> {
    
    private GraphDatabaseService database;
    private TransactionRegistry txs;
    
    public DatabaseWorkPerformer(GraphDatabaseService database, TransactionRegistry txs) {
        this.database = database;
        this.txs = txs;
    }

    @Override
    public void onEvent(DatabaseWork work) throws Exception 
    {
        try 
        {
            switch(work.getTransactionMode()) 
            {
            case OPEN_TRANSACTION:
                try {
                    txs.selectCurrentTransaction(work.getInvocation().getTxId());
                } catch(Throwable e) {
                    work.setFailed(e);
                }
                work.perform();
                break;
                
            case SINGLE_TRANSACTION:
                Transaction tx = null;
                try 
                {
                    txs.suspendCurrentTransaction();
                    tx = database.beginTx();
                } catch(Throwable e) 
                {
                    work.setFailed(e);
                }
                try
                {
                    work.perform();
                    if(tx != null) 
                    {
                        tx.success();
                    }
                } finally 
                {
                    if(tx != null) 
                    {
                        tx.finish();
                    }
                }
                break;
                
            case NO_TRANSACTION:
                try {
                    txs.suspendCurrentTransaction();
                } catch(Throwable e) {
                    work.setFailed(e);
                }
                work.perform();
                break;
            }
        } catch(Exception e) {
            // TODO: Logging
            e.printStackTrace();
        }
    }
 
//    private void dumpDb(final AbstractGraphDatabase gds) {
//        for (Node node : gds.getAllNodes()) {
//            System.out.println("node = " + node);
//            for (String prop : node.getPropertyKeys()) {
//                System.out.println(prop + ": "+node.getProperty(prop));
//            }
//        }
//    }
}
