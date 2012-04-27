package org.neo4j.smack.routing;

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
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.annotation.Transactional;
import org.neo4j.smack.serialization.strategy.NodeSerializationStrategy;
import org.neo4j.smack.serialization.strategy.PropertyContainerDeserializationStrategy;

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
    public void shouldPickUpTransactionalAnnotation() {
        Object annotatedObject = new Object() {

            @GET
            @Transactional
            @SuppressWarnings("unused")
            public void someRandomMethod() { }
            
            @GET
            @SuppressWarnings("unused")
            public void someOtherMethod() { }
            
        };
        
        AnnotationBasedRoutingDefinition rd = new AnnotationBasedRoutingDefinition(annotatedObject);
        
        Endpoint transactional = rd.getRouteDefinitionEntries().get(0).getEndpoint();
        Endpoint nontransactional = rd.getRouteDefinitionEntries().get(1).getEndpoint();
        
        assertThat(transactional.isTransactional(), is(true));
        assertThat(nontransactional.isTransactional(), is(false));
        
    }

    @Test
    public void shouldPickUpAppropriateSerializationAndDeserializationStrategies() {
        Object annotatedObject = new Object() {
            @GET
            @Path("/hello/world")
            @SuppressWarnings("unused")
            @SerializeWith(NodeSerializationStrategy.class)
            @DeserializeWith(PropertyContainerDeserializationStrategy.class)
            public void a() { }
        };
        
        AnnotationBasedRoutingDefinition rd = new AnnotationBasedRoutingDefinition(annotatedObject);
        RouteDefinitionEntry route = rd.getRouteDefinitionEntries().get(0);
        
        assertThat(route.getEndpoint().getSerializationStrategy(), is(NodeSerializationStrategy.class));
        assertThat(route.getEndpoint().getDeserializationStrategy(), is(PropertyContainerDeserializationStrategy.class));
    }
}
