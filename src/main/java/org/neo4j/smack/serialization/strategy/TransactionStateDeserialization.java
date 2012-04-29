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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.smack.serialization.IdentifiableEnum;

public enum TransactionStateDeserialization implements IdentifiableEnum {
    OPEN(0),
    COMMITTED(1),
    ROLLED_BACK(2);
    
    private static final Map<String, TransactionStateDeserialization> nameToValueMap = new HashMap<String, TransactionStateDeserialization>();
    private static final Map<Integer, TransactionStateDeserialization> idToValueMap = new HashMap<Integer, TransactionStateDeserialization>();

    static {
        for (TransactionStateDeserialization value : EnumSet.allOf(TransactionStateDeserialization.class)) {
            nameToValueMap.put(value.name(), value);
            idToValueMap.put(value.getId(), value);
        }
    }

    public static TransactionStateDeserialization getForId(int id) {
        return idToValueMap.get(id);
    }
    
    public static TransactionStateDeserialization getForName(String name) {
        return nameToValueMap.get(name);
    }
    
    private final int id;
    
    TransactionStateDeserialization(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
}