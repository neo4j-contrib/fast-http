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
package org.neo4j.smack.handler;

import com.lmax.disruptor.WorkHandler;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.neo4j.smack.event.ResponseEvent;
import org.neo4j.smack.serialization.SerializationFactory;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

public class SerializationHandler implements WorkHandler<ResponseEvent> {

    SerializationFactory serializationFactory = new SerializationFactory();

    @Override
    public void onEvent(ResponseEvent event) throws Exception {
        final Object data = event.getInvocationResult().getData();

        final HttpResponse httpResponse = event.getHttpResponse();
        if (data==null) {
            httpResponse.addHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);
        } else {
            @SuppressWarnings("unchecked") final SerializationStrategy<Object> serializationStrategy = (SerializationStrategy<Object>) event.getSerializationStrategy();
            final DynamicChannelBuffer content = new DynamicChannelBuffer(1000); // todo
    
            Serializer serializer = serializationFactory.getSerializer(content);
            serializationStrategy.serialize(data, serializer, null);
            httpResponse.setHeader(HttpHeaders.Names.CONTENT_TYPE, serializer.getContentType().toString());
            httpResponse.setContent(content);
        }
    }

}
