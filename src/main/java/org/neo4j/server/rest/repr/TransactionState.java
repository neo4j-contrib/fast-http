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
package org.neo4j.server.rest.repr;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum TransactionState implements IdentifiableEnum {
    OPEN(0),
    COMMITTED(1),
    ROLLED_BACK(2);
    
    private static final Map<String, TransactionState> nameToValueMap = new HashMap<String, TransactionState>();
    private static final Map<Integer, TransactionState> idToValueMap = new HashMap<Integer, TransactionState>();

    static {
        for (TransactionState value : EnumSet.allOf(TransactionState.class)) {
            nameToValueMap.put(value.name(), value);
            idToValueMap.put(value.getId(), value);
        }
    }

    public static TransactionState getForId(int id) {
        return idToValueMap.get(id);
    }
    
    public static TransactionState getForName(String name) {
        return nameToValueMap.get(name);
    }
    
    private final int id;
    
    TransactionState(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
}