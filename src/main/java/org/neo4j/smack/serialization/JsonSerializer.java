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
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class JsonSerializer implements Serializer {

    private final JsonGenerator generator;
    private final ChannelBufferOutputStream out;

    public JsonSerializer(JsonFactory jsonFactory, ChannelBuffer output) throws SerializationException {
        try {
            out = new ChannelBufferOutputStream(output);
            this.generator = jsonFactory.createJsonGenerator(out);
        } catch (IOException e) {
            throw new SerializationException("Error creating generator", e);
        }
    }

    @Override
    public void putEnum(Enum<?> en) throws SerializationException {

    }

    @Override
    public void putString(String string) throws SerializationException {
        try {
            generator.writeString(string);
        } catch (IOException e) {
            throw new SerializationException("Could node serialize String " + string, e);
        }
    }

    @Override
    public void putMap(Map<String, Object> data) throws SerializationException {
        try {
            generator.writeObject(data);
        } catch (IOException e) {
            throw new SerializationException("Could not serialize map " + data, e);
        }
    }

    @Override
    public void putNode(Node node) throws SerializationException {
        // todo output elements one by one to json reduce overhead
        putMap(GraphElementSerializer.toNodeMap(node));
    }

    @Override
    public void putRelationship(Relationship rel) throws SerializationException {
        putMap(GraphElementSerializer.toRelationshipMap(rel));
    }

    @Override
    public void putRaw(String data) throws SerializationException {
        try {
            out.writeBytes(data);
        } catch (IOException e) {
            throw new SerializationException("Could not serialize string to bytes " + data, e);
        }
    }

    @Override
    public MediaType getContentType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }
}
