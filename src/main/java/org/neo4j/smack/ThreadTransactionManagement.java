package org.neo4j.smack;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.neo4j.smack.event.WorkTransactionMode;

public class ThreadTransactionManagement {
    
    private final TransactionRegistry txs;

    public ThreadTransactionManagement(TransactionRegistry txs) 
    {
        this.txs = txs;
    }

    public void beforeWork(WorkTransactionMode txMode, long txId) throws SystemException, InvalidTransactionException
    {
        switch(txMode) 
        {
        case OPEN_TRANSACTION:
            txs.selectCurrentTransaction(txId);
            break;
            
        case SINGLE_TRANSACTION:
            txs.createTransaction(txId);
            txs.selectCurrentTransaction(txId);
            break;
            
        case NO_TRANSACTION:
            txs.suspendCurrentTransaction();
            break;
        }
    }

    public void afterWork(WorkTransactionMode txMode, long txId) throws InvalidTransactionException, RollbackException, SystemException, IllegalStateException, SecurityException, HeuristicMixedException, HeuristicRollbackException
    {
        switch(txMode) 
        {
        case SINGLE_TRANSACTION:
            txs.commitCurrentTransaction();
            break;

        case OPEN_TRANSACTION:   
        case NO_TRANSACTION:
            break;
        }
    }

    public void onWorkFailure(WorkTransactionMode txMode, long txId) throws InvalidTransactionException, IllegalStateException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException
    {
        switch(txMode) 
        {
        case OPEN_TRANSACTION:
        case SINGLE_TRANSACTION:
            txs.rollbackCurrentTransaction();
            break;
            
        case NO_TRANSACTION:
            break;
        }
    }

}
