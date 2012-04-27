package org.neo4j.smack.routing;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Output;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.SerializationStrategy;

public class TestRouter  {

    class Routling implements Routable {
        private String path;
        private InvocationVerb verb;
        private PathVariables pathVariables = new PathVariables();

        Routling(InvocationVerb verb, String path) {
            this.verb = verb;
            this.path = path;
        }

        @Override
        public String getPath()
        {
            return path;
        }

        @Override
        public InvocationVerb getVerb()
        {
            return verb;
        }

        @Override
        public PathVariables getPathVariables()
        {
            return pathVariables;
        }
    }
    
    @Test
    public void shouldRouteVerbsCorrectly() {
       Endpoint e = new Endpoint() {

            @Override
            public void invoke(Invocation ctx, Output result) throws Exception { }

            @Override
            public InvocationVerb getVerb() {
                return InvocationVerb.GET;
            }

            @Override
            public DeserializationStrategy<?> getDeserializationStrategy() {
                return DeserializationStrategy.NO_OP;
            }

            public SerializationStrategy<?> getSerializationStrategy() {
                return SerializationStrategy.NO_OP;
            }

            public boolean isTransactional() {
                return false;
            }
        };
        
        Router r = new Router();
        r.addRoute("/db/data", e);
        r.compileRoutes();
        
        Endpoint found = r.route(new Routling(InvocationVerb.GET, "/db/data"));
        assertNotNull(found);
        
        Endpoint endpoint = r.route(new Routling(InvocationVerb.POST, "/db/data"));
        assertThat(endpoint, instanceOf(NotFoundEndpoint.class));
    }

    @Test
    public void shouldRouteSimplePathsCorrectly() {
       Endpoint e = new Endpoint() {

            @Override
            public void invoke(Invocation ctx,
                    Output response) throws Exception { }

            @Override
            public InvocationVerb getVerb() {
                return InvocationVerb.GET;
            }

            @Override
            public DeserializationStrategy<?> getDeserializationStrategy() {
                return DeserializationStrategy.NO_OP;
            }

            public SerializationStrategy<?> getSerializationStrategy() {
                return SerializationStrategy.NO_OP;
            }

            public boolean isTransactional() {
                return false;
            }
        };
        
        Router r = new Router();
        r.addRoute("/db/data", e);
        r.compileRoutes();
        
        Endpoint found = r.route(new Routling(InvocationVerb.GET, "/db/data"));
        assertThat(found, is(e));
        
        Endpoint endpoint = r.route(new Routling(InvocationVerb.GET, "/db/da"));
        assertThat(endpoint, instanceOf(NotFoundEndpoint.class)); 
    }

    @Test
    public void shouldRoutePathsWithParamsProperly() {
       Endpoint e = new SimpleEndpoint() {

            @Override
            public void invoke(Invocation ctx,
                    Output response) throws Exception { }

        };
        
        Router r = new Router();
        r.addRoute("/db/data/{prop}", e);
        r.compileRoutes();
        
        Endpoint found = r.route(new Routling(InvocationVerb.GET, "/db/data/somestuff"));
        assertThat(found, is(e));
        
        Endpoint endpoint = r.route(new Routling(InvocationVerb.GET, "/db/data/somestuff/whaa"));
        assertThat(endpoint, instanceOf(NotFoundEndpoint.class)); 
    }
    
}
