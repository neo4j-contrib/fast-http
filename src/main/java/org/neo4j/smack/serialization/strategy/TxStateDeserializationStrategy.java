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
package org.neo4j.smack.serialization.strategy;

import org.neo4j.smack.domain.TransactionState;
import org.neo4j.smack.serialization.DeserializationException;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.Deserializer;
import org.neo4j.smack.serialization.IdentifiableEnumDeserializer;

public class TxStateDeserializationStrategy implements DeserializationStrategy<TransactionState>, IdentifiableEnumDeserializer<TransactionState> {

    public TxStateDeserializationStrategy() {}
    
    public TransactionState deserialize(Deserializer in) throws DeserializationException {
        return in.readEnum(this);
    }

    @Override
    public TransactionState getForId(int id) {
        return TransactionState.getForId(id);
    }

    @Override
    public TransactionState getForName(String name) {
        return TransactionState.getForName(name);
    }
}
