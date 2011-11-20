package org.neo4j.server.smack;

import static org.junit.Assert.assertNotNull;

import java.lang.annotation.Annotation;

import org.junit.Test;
import org.neo4j.server.smack.core.RequestEvent;
import org.neo4j.server.smack.serialization.DeserializationStrategy;
import org.neo4j.server.smack.serialization.SerializationStrategy;

public class TestRouter  {

    @Test
    public void shouldRouteVerbsCorrectly() {
       Endpoint e = new Endpoint() {

            @Override
            public void invoke(InvocationRequest ctx,
                    InvocationResponse response) throws Exception { }

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

            public boolean hasAnnotation(
                    Class<? extends Annotation> annotationClass) {
                return false;
            }
        };
        
        Router r = new Router();
        r.addRoute("/db/data", e);
        r.compileRoutes();
        
        RequestEvent req = new RequestEvent();
        req.setVerb(InvocationVerb.GET);
        req.setPath("/db/data");
        
        Endpoint found = r.route(req);
        assertNotNull(found);
        
        ResourceNotFoundException ex = null;
        try {
            req = new RequestEvent();
            req.setVerb(InvocationVerb.POST);
            req.setPath("/db/data");
            
            r.route(req);
        } catch(ResourceNotFoundException rx) {
            ex = rx;
        }
        assertNotNull(ex);   
    }

    @Test
    public void shouldRouteSimplePathsCorrectly() {
       Endpoint e = new Endpoint() {

            @Override
            public void invoke(InvocationRequest ctx,
                    InvocationResponse response) throws Exception { }

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

            public boolean hasAnnotation(
                    Class<? extends Annotation> annotationClass) {
                return false;
            }
        };
        
        Router r = new Router();
        r.addRoute("/db/data", e);
        r.compileRoutes();
        
        RequestEvent req = new RequestEvent();
        req.setVerb(InvocationVerb.GET);
        req.setPath("/db/data");
        
        Endpoint found = r.route(req);
        assertNotNull(found);
        
        ResourceNotFoundException ex = null;
        try {
            req = new RequestEvent();
            req.setVerb(InvocationVerb.GET);
            req.setPath("/db/da");
            
            r.route(req);
        } catch(ResourceNotFoundException rx) {
            ex = rx;
        }
        assertNotNull(ex);   
    }
    
}
