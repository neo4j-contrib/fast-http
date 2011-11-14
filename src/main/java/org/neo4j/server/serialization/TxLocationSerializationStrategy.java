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
package org.neo4j.server.serialization;

import org.neo4j.server.smack.serialization.DeserializationStrategy;
import org.neo4j.server.smack.serialization.Deserializer;
import org.neo4j.server.smack.serialization.SerializationModifier;
import org.neo4j.server.smack.serialization.Serializer;

public class TxLocationSerializationStrategy implements DeserializationStrategy<Long> {

    public void serialize(Long txId, Serializer out, SerializationModifier modifier) {
        out.putString("/db/data/tx/" + txId);
    }

    public Long deserialize(Deserializer out) {
        return null;
    }
    
}
