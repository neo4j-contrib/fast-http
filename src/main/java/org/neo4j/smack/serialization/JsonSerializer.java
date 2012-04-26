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


import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;

public class JsonSerializer implements Serializer {

    private final JsonGenerator generator;

    public JsonSerializer(JsonFactory jsonFactory, ChannelBuffer output) {
        try {
            generator = jsonFactory.createJsonGenerator(new ChannelBufferOutputStream(output));
            
        } catch (IOException e) {
            throw new SerializationException("Error creating generator", e);
        }
    }
    
    public JsonSerializer(JsonFactory jsonFactory, OutputStream output) {
        try {
            this.generator = jsonFactory.createJsonGenerator(output);
        } catch (IOException e) {
            throw new SerializationException("Error creating generator", e);
        }
    }

    @Override
    public void putEnum(Enum<?> en) {

    }

    @Override
    public void putString(String string) {
        try {
            generator.writeString(string);
        } catch (IOException e) {
            throw new SerializationException("Could node serialize String " + string, e);
        }
    }

    @Override
    public void putInteger(int value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new SerializationException("Could node serialize integer " + value, e);
        }
    }

    @Override
    public void putLong(long value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new SerializationException("Could node serialize long " + value, e);
        }
    }

    @Override
    public void putFloat(float value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new SerializationException("Could node serialize float " + value, e);
        }
    }

    @Override
    public void putDouble(double value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new SerializationException("Could node serialize double " + value, e);
        }
    }

    @Override
    public void putBoolean(boolean value) {
        try {
            generator.writeBoolean(value);
        } catch (IOException e) {
            throw new SerializationException("Could node serialize boolean " + value, e);
        }
    }

    @Override
    public void putMap(Map<String, Object> data) {
        try {
            generator.writeObject(data);
        } catch (IOException e) {
            throw new SerializationException("Could not serialize map " + data, e);
        }
    }

//    @Override
//    public void putRaw(String data) {
//        try {
//            out.writeBytes(data);
//        } catch (IOException e) {
//            throw new SerializationException("Could not serialize string to bytes " + data, e);
//        }
//    }

    @Override
    public void startList()
    {
        try {
            generator.writeStartArray();
        } catch (IOException e) {
            throw new SerializationException("Could not start list.", e);
        }
    }

    @Override
    public void endList()
    {
        try {
            generator.writeEndArray();
        } catch (IOException e) {
            throw new SerializationException("Could not end list.", e);
        }
    }
    
    @Override
    public void startMap()
    {
        try {
            generator.writeStartObject();
        } catch (IOException e) {
            throw new SerializationException("Could not start map.", e);
        }
    }

    @Override
    public void putMapPropertyName(String string)
    {
        try {
            generator.writeFieldName(string);
        } catch (IOException e) {
            throw new SerializationException("Could not write map key", e);
        }
    }

    @Override
    public void endMap()
    {
        try {
            generator.writeEndObject();
        } catch (IOException e) {
            throw new SerializationException("Could not end map.", e);
        }
    }

    @Override
    public MediaType getContentType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public void flush()
    {
        try {
            generator.flush();
        } catch(IOException e) {
            throw new SerializationException("Flush failed.", e);
        }
    }
}
