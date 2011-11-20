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
package org.neo4j.server.rest.service;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.neo4j.server.annotations.Transactional;
import org.neo4j.server.rest.repr.TransactionState;
import org.neo4j.server.serialization.TxStateDeserializationStrategy;
import org.neo4j.server.smack.InvocationRequest;
import org.neo4j.server.smack.InvocationResponse;
import org.neo4j.server.smack.annotations.DeserializeWith;
import org.neo4j.server.transaction.TransactionRegistry;

public class TransactionService {

    private final TransactionServiceActions actions;

    public TransactionService() {
        this(new TransactionServiceActions());
    }
    
    public TransactionService(TransactionServiceActions actions) {
        this.actions = actions;
    }
    
    @POST
    public void createTransaction(InvocationRequest req, InvocationResponse res) {
        TransactionRegistry txs = req.getCtx(ContextKeys.TX_REGISTRY);
        Long txId = req.getCtx(ContextKeys.TX_ID);
        
        actions.createTransaction(txs, txId);
        
        res.setCreated("/db/data/tx/" + txId);
    }
    
    @PUT
    @Path("/{tx_id}/state")
    @DeserializeWith(TxStateDeserializationStrategy.class)
    @Transactional
    public void setTransactionState(InvocationRequest req, InvocationResponse res) throws Exception {
        TransactionRegistry database = req.getCtx(ContextKeys.TX_REGISTRY);
        Long txId = req.getCtx(ContextKeys.TX_ID);
        
        actions.setTransactionState(database, txId, (TransactionState)req.getDeserializedContent());
        
        res.setOk();
    }
    
}
