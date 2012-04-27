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
package org.neo4j.smack.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
public class JsonDeserializer implements Deserializer {

    private JsonParser parser;

    public JsonDeserializer(JsonFactory factory, InputStream stream) {
        try {
            this.parser = factory.createJsonParser(stream);
        } catch (JsonParseException e) {
            throw new DeserializationException("Unable to instantiate JSON parser.", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DeserializationException("Unable to instantiate JSON parser.", e);
        }
    }
    
    @Override
    public long readLong() {
        try {
            return parser.getLongValue();
        } catch (Exception e) {
            throw new DeserializationException("Invalid JSON format, expected Long.", e);
        }
    }

    @Override
    public long readInt() {
        try {
            return parser.getIntValue();
        } catch (Exception e) {
            throw new DeserializationException("Invalid JSON format, expected Integer.", e);
        }
    }

    @Override
    public <T> T readEnum(IdentifiableEnumDeserializer<T> deserializer) {
        String str = readString();
        return deserializer.getForName(str);
    }

    @Override
    public String readString() {
        try {
            parser.nextValue();
            return parser.getText();
        } catch (JsonParseException e) {
            throw new DeserializationException("Invalid JSON format, expected String.", e);
        } catch (IOException e) {
            throw new DeserializationException("Unable to read expected String value.", e);
        }
    }

    @Override
    public Object readObject() {
        try {
            return parser.readValueAs(Object.class);
        } catch(EOFException e) {
            return null;
        } catch (JsonParseException e) {
            throw new DeserializationException("Invalid JSON format, expected object.", e);
        } catch (IOException e) {
            throw new DeserializationException("Unable to read expected object value.", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> readMap() {
        try {
            return parser.readValueAs(Map.class);
        } catch (JsonParseException e) {
            throw new DeserializationException("Invalid JSON format, expected Map.", e);
        } catch (IOException e) {
            throw new DeserializationException("Unable to read expected Map value.", e);
        }
    }
}
