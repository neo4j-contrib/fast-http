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
package org.neo4j.smack.routing;

import java.lang.reflect.Method;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.annotation.Transactional;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.NettyChannelBackedOutput;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.SerializationStrategy;

public class AnnotationBasedRoutingDefinition extends RoutingDefinition {

    private final Object underlyingObject;

    private class MethodInvokingEndpoint implements Endpoint {

        private final Object underlyingObject;
        private final Method method;
        private final InvocationVerb verb;
        private final boolean isTransactional;
        private final SerializationStrategy<?> serializationStrategy;
        private final DeserializationStrategy<?> deserializationStrategy;

        public MethodInvokingEndpoint(InvocationVerb verb, Method method,
                boolean isTransactional, Object underlyingObject,
                SerializationStrategy<?> serializationStrategy,
                DeserializationStrategy<?> deserializationStrategy)
        {
            this.verb = verb;
            this.method = method;
            this.isTransactional = isTransactional;
            this.underlyingObject = underlyingObject;
            this.serializationStrategy = serializationStrategy;
            this.deserializationStrategy = deserializationStrategy;
        }

        public void invoke(Invocation request, NettyChannelBackedOutput result) throws Exception
        {
            method.invoke(underlyingObject, request, result);
        }

        @Override
        public String toString()
        {
            return String.format("MethodInvokingEndpoint{method=%s, verb=%s}",
                    method, verb);
        }

        @Override
        public InvocationVerb getVerb()
        {
            return verb;
        }

        @Override
        public boolean isTransactional()
        {
            return isTransactional;
        }

        @Override
        public DeserializationStrategy<?> getDeserializationStrategy()
        {
            return deserializationStrategy;
        }

        @Override
        public SerializationStrategy<?> getSerializationStrategy()
        {
            return serializationStrategy;
        }
    }

    public AnnotationBasedRoutingDefinition(Object obj)
    {
        this.underlyingObject = obj;
        setupRoutes();
    }

    private void setupRoutes()
    {
        for (Method m : underlyingObject.getClass().getMethods())
        {

            if (m.isAnnotationPresent(GET.class))
            {
                addRoute(m, InvocationVerb.GET);
            }

            if (m.isAnnotationPresent(PUT.class))
            {
                addRoute(m, InvocationVerb.PUT);
            }

            if (m.isAnnotationPresent(POST.class))
            {
                addRoute(m, InvocationVerb.POST);
            }

            if (m.isAnnotationPresent(DELETE.class))
            {
                addRoute(m, InvocationVerb.DELETE);
            }

            if (m.isAnnotationPresent(HEAD.class))
            {
                addRoute(m, InvocationVerb.HEAD);
            }

        }
    }

    private void addRoute(final Method method, final InvocationVerb verb)
    {

        String path = "";
        boolean isTransactional = false;
        SerializationStrategy<?> serializationStrategy = SerializationStrategy.NO_OP;
        DeserializationStrategy<?> deserializationStrategy = DeserializationStrategy.NO_OP;

        try
        {

            method.setAccessible(true);

            if (method.isAnnotationPresent(Path.class))
            {
                path = method.getAnnotation(Path.class).value();
            }

            if (method.isAnnotationPresent(SerializeWith.class))
            {
                serializationStrategy = (SerializationStrategy<?>) method
                        .getAnnotation(SerializeWith.class).value()
                        .getConstructor().newInstance();
            }

            if (method.isAnnotationPresent(DeserializeWith.class))
            {
                deserializationStrategy = (DeserializationStrategy<?>) method
                        .getAnnotation(DeserializeWith.class).value()
                        .getConstructor().newInstance();
            }

            if (method.isAnnotationPresent(Transactional.class))
            {
                isTransactional = true;
            }

            Endpoint endpoint = new MethodInvokingEndpoint(verb, method,
                    isTransactional, underlyingObject, serializationStrategy,
                    deserializationStrategy);

            if (path.isEmpty() || path.startsWith("/"))
            {
                addRoute(path, endpoint);
            } else
            {
                addRoute("/" + path, endpoint);
            }

        } catch (NoSuchMethodException e)
        {
            throw new RuntimeException(
                    "Serialization/deserialization strategies must implement no-arg constructor.",
                    e);
        } catch (Exception e)
        {
            throw new RuntimeException(
                    "Unable to create service from annotated class for path '"
                            + path + "'.", e);
        }
    }

}
