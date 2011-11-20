package org.neo4j.server.smack;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.junit.Test;
import org.neo4j.server.annotations.Transactional;

public class TestAnnotationBasedRoutingDefinition {

    @Test
    public void shouldPickUpJaxRSVerbAnnotations() {
        Object annotatedObject = new Object() {

            @GET
            @SuppressWarnings("unused")
            public void a() { }
            
            @POST
            @SuppressWarnings("unused")
            public void b() { }
            
            @PUT
            @SuppressWarnings("unused")
            public void c() { }

            @DELETE
            @SuppressWarnings("unused")
            public void d() { }

            @HEAD
            @SuppressWarnings("unused")
            public void e() { }
        };
        
        AnnotationBasedRoutingDefinition rd = new AnnotationBasedRoutingDefinition(annotatedObject);
        
        List<RouteDefinitionEntry> entries = rd.getRouteDefinitionEntries();
        
        assertThat(entries.size(), is(5));
        assertThat(entries.get(0).getPath(), is(""));
    }

    @Test
    public void shouldPickupPathAnnotation() {
        Object annotatedObject = new Object() {
            @GET
            @Path("/hello/world")
            @SuppressWarnings("unused")
            public void a() { }
        };
        
        AnnotationBasedRoutingDefinition rd = new AnnotationBasedRoutingDefinition(annotatedObject);
        
        List<RouteDefinitionEntry> entries = rd.getRouteDefinitionEntries();
        
        assertThat(entries.size(), is(1));
        assertThat(entries.get(0).getPath(), is("/hello/world"));
    }

    @Test
    public void shouldPickUpArbitraryMethodAnnotations() {
        Object annotatedObject = new Object() {

            @SuppressWarnings("unused")
            @Transactional
            @GET
            public void someRandomMethod() { }
            
        };
        
        AnnotationBasedRoutingDefinition rd = new AnnotationBasedRoutingDefinition(annotatedObject);
        
        Endpoint p = rd.getRouteDefinitionEntries().get(0).getEndpoint();
        
        assertThat(p.hasAnnotation(Transactional.class), is(true));
        
    }
}
