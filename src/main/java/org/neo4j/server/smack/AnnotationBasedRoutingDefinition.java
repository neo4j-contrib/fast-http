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
package org.neo4j.server.smack;

import org.neo4j.server.smack.annotations.DeserializeWith;
import org.neo4j.server.smack.annotations.Parameters;
import org.neo4j.server.smack.annotations.SerializeWith;
import org.neo4j.server.smack.serialization.DeserializationStrategy;
import org.neo4j.server.smack.serialization.NoOpDeserializationStrategy;
import org.neo4j.server.smack.serialization.NoOpSerializationStrategy;
import org.neo4j.server.smack.serialization.SerializationStrategy;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnotationBasedRoutingDefinition extends RoutingDefinition {

    private final Object underlyingObject;

    private class MethodInvokingEndpoint implements Endpoint {

        private final Object underlyingObject;
        private final Method method;
        private final InvocationVerb verb;
        private final Map<Object, Object> parameters;
        private final SerializationStrategy<?> serializationStrategy;
        private final DeserializationStrategy<?> deserializationStrategy;

        public MethodInvokingEndpoint(InvocationVerb verb, Method method,
                Object underlyingObject, Map<Object, Object> parameters,
                SerializationStrategy<?> serializationStrategy,
                DeserializationStrategy<?> deserializationStrategy) {
            this.verb = verb;
            this.method = method;
            this.underlyingObject = underlyingObject;
            this.parameters = parameters;
            this.serializationStrategy = serializationStrategy;
            this.deserializationStrategy = deserializationStrategy;
        }

        public void invoke(InvocationRequest request,
                InvocationResponse response) throws Exception {
            method.invoke(underlyingObject, request, response);
        }

        @Override
        public String toString() {
            return String.format("MethodInvokingEndpoint{method=%s, verb=%s}", method, verb);
        }

        @Override
        public InvocationVerb getVerb() {
            return verb;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getParameter(Object key) {
            if (parameters.containsKey(key))
                return (T) parameters.get(key);
            return null;
        }

        @Override
        public DeserializationStrategy<?> getDeserializationStrategy() {
            return deserializationStrategy;
        }

        @Override
        public SerializationStrategy<?> getSerializationStrategy() {
            return serializationStrategy;
        }
    }

    public AnnotationBasedRoutingDefinition(Object obj) {
        this.underlyingObject = obj;
    }

    public void setupRoutes() {
        for (Method m : underlyingObject.getClass().getMethods()) {

            if (m.isAnnotationPresent(GET.class)) {
                addRoute(m, InvocationVerb.GET);
            }

            if (m.isAnnotationPresent(PUT.class)) {
                addRoute(m, InvocationVerb.PUT);
            }

            if (m.isAnnotationPresent(POST.class)) {
                addRoute(m, InvocationVerb.POST);
            }

            if (m.isAnnotationPresent(DELETE.class)) {
                addRoute(m, InvocationVerb.DELETE);
            }

        }
    }

    private void addRoute(final Method method, final InvocationVerb verb) {
        String path = null;
        try {
            path = "";
            if (method.isAnnotationPresent(Path.class)) {
                path = method.getAnnotation(Path.class).value();
            }

            Map<Object, Object> parameters = new HashMap<Object, Object>();
            if (method.isAnnotationPresent(Parameters.class)) {
                for (String paramDef : method.getAnnotation(Parameters.class)
                        .value()) {
                    String[] parts = paramDef.split(":");
                    if (parts.length != 2) {
                        throw new RuntimeException(
                                "Invalid parameter definition,'" + paramDef
                                        + "', expected 'key:value'.");
                    }
                    parameters.put(parts[0], parts[1]);
                }
            }

            SerializationStrategy<?> serializationStrategy = new NoOpSerializationStrategy();
            DeserializationStrategy<?> deserializationStrategy = new NoOpDeserializationStrategy();
                
            if (method.isAnnotationPresent(SerializeWith.class)) {
                serializationStrategy = (SerializationStrategy<?>) method
                        .getAnnotation(SerializeWith.class).value()
                        .getConstructor().newInstance();
            }

            if (method.isAnnotationPresent(DeserializeWith.class)) {
                deserializationStrategy = (DeserializationStrategy<?>) method
                        .getAnnotation(DeserializeWith.class).value()
                        .getConstructor().newInstance();
            }

            addRoute(path, new MethodInvokingEndpoint(verb, method,
                    underlyingObject, parameters, serializationStrategy,
                    deserializationStrategy));
            
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Serialization/deserialization strategies must implement no-arg constructor.", e);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to create service from annotated class for path '"
                            + path + "'.", e);
        }
    }

}
