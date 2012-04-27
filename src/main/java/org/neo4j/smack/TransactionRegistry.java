/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.smack;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.AbstractGraphDatabase;

public class TransactionRegistry {
    private TransactionManager tm;
    private GraphDatabaseService db;
    
    private long currentTxId = -1l;
    
    private Map<Long, Transaction> txIdToTxMap = new HashMap<Long, Transaction>();
    
    /**
     * Using this constructor requires setting the the transaction manager via
     * {@link #setTransactionManager(TransactionManager)} method.
     */
    public TransactionRegistry() {

    }

    public TransactionRegistry(GraphDatabaseService neo4j) {
        this.db = neo4j;
        this.tm = ((AbstractGraphDatabase) neo4j).getConfig().getTxModule()
                .getTxManager();
    }

    public void selectCurrentTransaction(long txId)
            throws InvalidTransactionException, IllegalStateException,
            SystemException 
    {
        if(currentTxId != txId) 
        {
            suspendCurrentTransaction();
            Transaction tx = txIdToTxMap.get(txId);
            if (tx == null) {
                throw new InvalidTransactionException("No transaction with id "
                        + txId + " found.");
            }
            tm.resume(tx);
            currentTxId = txId;
        }
    }

    public void suspendCurrentTransaction() throws SystemException 
    {
        if(currentTxId != -1l) 
        {
            try 
            {
                tm.suspend();
            } finally 
            {
                currentTxId = -1l;
            }
        }
    }
    
    public void createTransaction(long id) 
    {
        org.neo4j.graphdb.Transaction neo4jTx = db.beginTx();
        try {
            Transaction tx = tm.suspend();
            txIdToTxMap.put(id, tx);
        } catch (Exception e) {
            neo4jTx.finish();
            throw new RuntimeException(e);
        }
    }

    public void commitCurrentTransaction() throws IllegalStateException,
            SecurityException, HeuristicMixedException,
            HeuristicRollbackException, RollbackException, SystemException,
            InvalidTransactionException 
    {
        if(currentTxId != -1l) 
        {
            try {
                tm.commit();
            } finally {
                txIdToTxMap.remove(currentTxId);
                currentTxId = -1l;
            }
        } else {
            throw new InvalidTransactionException("Can't commit, no transaction selected.");
        }
    }

    public void rollbackCurrentTransaction() throws IllegalStateException,
            SecurityException, HeuristicMixedException,
            HeuristicRollbackException, RollbackException, SystemException,
            InvalidTransactionException 
    {
        if(currentTxId != -1l) 
        {
            try {
                tm.rollback();
            } finally {
                txIdToTxMap.remove(currentTxId);
                currentTxId = -1l;
            }
        } else {
            throw new InvalidTransactionException("Can't roll back, no transaction selected.");
        }

    }

    public void setTransactionTimeout(int sec) throws SystemException {
        tm.setTransactionTimeout(sec);
    }

    public void setTransactionManager(TransactionManager tm) {
        this.tm = tm;
    }

    public TransactionManager getTransactionManager() {
        return tm;
    }

}
