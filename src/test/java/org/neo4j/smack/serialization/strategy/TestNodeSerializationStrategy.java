package org.neo4j.smack.serialization.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.jackson.JsonFactory;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.smack.serialization.JsonSerializer;

public class TestNodeSerializationStrategy {

    @Test
    public void shouldSerializeNodeWithNoProperties() {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonSerializer serializer = new JsonSerializer(new JsonFactory(), buffer);
        
        Node mockNode = mock(Node.class);
        when(mockNode.getId()).thenReturn(0l);
        when(mockNode.getPropertyKeys()).thenReturn(Collections.<String> emptyList());
        
        NodeSerializationStrategy strategy = new NodeSerializationStrategy();
        strategy.serialize(mockNode, serializer);
        
        assertThat(new String(buffer.toByteArray()),is("{" +
                "\"data\":{}," +
                "\"self\":\"/db/data/node/0\"," +
                "\"extensions\":{}" +
            "}"));
        
        
    }
    
    @Test
    public void shouldSerializeNodeWithProperties() {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonSerializer serializer = new JsonSerializer(new JsonFactory(), buffer);
        
        Node mockNode = mock(Node.class);
        when(mockNode.getId()).thenReturn(0l);
        when(mockNode.getPropertyKeys()).thenReturn(new ArrayList<String>() {
            private static final long serialVersionUID = 1248180220593789023L;
        {
            add("name");
            add("age");
            add("favorite_numbers");
        }});
        
        when(mockNode.getProperty("name")).thenReturn("bob");
        when(mockNode.getProperty("age")).thenReturn(12);
        when(mockNode.getProperty("favorite_numbers")).thenReturn(new int [] {1,2,3});
        
        NodeSerializationStrategy strategy = new NodeSerializationStrategy();
        strategy.serialize(mockNode, serializer);
        
        assertThat(new String(buffer.toByteArray()),is(
                "{" +
                  "\"data\":{" +
                    "\"name\":\"bob\"," +
                    "\"age\":12," +
                    "\"favorite_numbers\":[1,2,3]" +
                  "}," +
                  "\"self\":\"/db/data/node/0\"," +
                  "\"extensions\":{}" +
                "}"));
    }
    
}
