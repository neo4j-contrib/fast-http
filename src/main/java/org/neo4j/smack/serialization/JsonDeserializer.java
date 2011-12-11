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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
public class JsonDeserializer implements Deserializer {

    private JsonParser parser;

    public JsonDeserializer(JsonFactory factory, InputStream stream) throws DeserializationException {
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
    public long readLong() throws DeserializationException {
        try {
            return parser.getLongValue();
        } catch (Exception e) {
            throw new DeserializationException("Invalid JSON format, expected Long.", e);
        }
    }

    @Override
    public long readInt() throws DeserializationException {
        try {
            return parser.getIntValue();
        } catch (Exception e) {
            throw new DeserializationException("Invalid JSON format, expected Integer.", e);
        }
    }

    @Override
    public <T> T readEnum(IdentifiableEnumDeserializer<T> deserializer) throws DeserializationException {
        String str = readString();
        return deserializer.getForName(str);
    }

    @Override
    public String readString() throws DeserializationException {
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
    public Object readObject() throws DeserializationException {
        try {
            return parser.readValueAs(Object.class);
        } catch (JsonParseException e) {
            throw new DeserializationException("Invalid JSON format, expected object.", e);
        } catch (IOException e) {
            throw new DeserializationException("Unable to read expected object value.", e);
        }
    }


    @Override
    public Map<String, Object> readMap() throws DeserializationException {
        try {
            Map<String, Object> map = new HashMap<String, Object>();

            String field;
            JsonToken token;
            token = parser.nextToken();
            
            if(token == JsonToken.START_OBJECT) {
                while( (token = parser.nextToken()) != JsonToken.END_OBJECT && token != null) {
                    field = parser.getText();
                    token = parser.nextToken();
                    map.put(field, parser.readValueAs(Object.class));
                }
            } else {
                throw new DeserializationException("Invalid JSON, expected map.");
            }
            return map;
        } catch (JsonParseException e) {
            throw new DeserializationException("Invalid JSON format, expected String.", e);
        } catch (IOException e) {
            throw new DeserializationException("Unable to read expected String value.", e);
        }
    }
}
