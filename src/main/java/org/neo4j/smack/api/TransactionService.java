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

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.Transactional;
import org.neo4j.smack.domain.TransactionState;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Output;
import org.neo4j.smack.serialization.strategy.TxStateDeserializationStrategy;

public class TransactionService {

    private final TransactionServiceActions actions;

    public TransactionService() {
        this(new TransactionServiceActions());
    }
    
    public TransactionService(TransactionServiceActions actions) {
        this.actions = actions;
    }
    
    @POST
    @Path("")
    public void createTransaction(Invocation req, Output res) {
        TransactionRegistry txs = req.getTxRegistry();
        Long txId = req.getTxId();
        
        actions.createTransaction(txs, txId);
        
        res.createdAt("/db/data/tx/" + txId);
    }
    
    @PUT
    @Path("/{tx_id}/state")
    @DeserializeWith(TxStateDeserializationStrategy.class)
    @Transactional
    public void setTransactionState(Invocation req, Output res) throws Exception {
        TransactionRegistry database = req.getTxRegistry();
        Long txId = req.getTxId();
        
        actions.setTransactionState(database, txId, (TransactionState)req.getContent());
        
        res.ok();
    }
}
