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
package org.neo4j.smack.api;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.domain.TransactionState;

public class TransactionServiceActions {

    public void createTransaction(TransactionRegistry txs, long txId) {
        txs.createTransaction(txId);
    }
    
    public void setTransactionState(TransactionRegistry txs, long txId, TransactionState state) throws InvalidTransactionException, IllegalStateException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
        switch(state) {
        case COMMITTED:
            txs.commit(txId);
            break;
        case ROLLED_BACK:        
            txs.rollback(txId);
            break;
        default:
            throw new IllegalArgumentException("Only COMMITTED and ROLLED_BACK transaction states can be set.");
        }
    }
}
