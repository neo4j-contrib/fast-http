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

import org.neo4j.smack.domain.TransactionState;
import org.neo4j.smack.pipeline.database.TransactionRegistry;
import org.neo4j.smack.pipeline.database.event.Invocation;
import org.neo4j.smack.pipeline.database.event.Output;
import org.neo4j.smack.routing.annotation.DeserializeWith;
import org.neo4j.smack.routing.annotation.Transactional;
import org.neo4j.smack.serialization.strategy.TxStateDeserializationStrategy;

public class TransactionService {
    
    private static final UrlReverseLookerUpper url = new UrlReverseLookerUpper();
    
    @POST
    @Path("")
    public void createTransaction(Invocation req, Output res) {
        TransactionRegistry txs = req.getTxRegistry();
        Long txId = req.getTxId();
        
        txs.createTransaction(txId);
        
        res.createdAt(url.reverseTransaction(txId));
    }
    
    @PUT
    @Path("/{tx_id}/state")
    @Transactional
    @DeserializeWith(TxStateDeserializationStrategy.class)
    public void setTransactionState(Invocation req, Output res) throws Exception {
        TransactionRegistry txs = req.getTxRegistry();
        
        switch(req.<TransactionState>getContent()) {
        case COMMITTED:
            txs.commitCurrentTransaction();
            break;
        case ROLLED_BACK:
            txs.rollbackCurrentTransaction();
            break;
        default:
            throw new IllegalArgumentException("Only COMMITTED and ROLLED_BACK transaction states can be set.");
        }
        
        res.ok();
    }
}
