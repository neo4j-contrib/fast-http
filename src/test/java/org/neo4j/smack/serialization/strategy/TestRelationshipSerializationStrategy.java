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
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.smack.serialization.JsonSerializer;


public class TestRelationshipSerializationStrategy {

    @Test
    public void shouldSerializeRelationshipWithNoProperties() {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonSerializer serializer = new JsonSerializer(new JsonFactory(), buffer);
        
        Node fromNode = mock(Node.class);
        when(fromNode.getId()).thenReturn(0l);
        
        Node toNode = mock(Node.class);
        when(toNode.getId()).thenReturn(1l);
        
        Relationship mockRel = mock(Relationship.class);
        when(mockRel.getId()).thenReturn(0l);
        when(mockRel.getType()).thenReturn(DynamicRelationshipType.withName("LOVES"));
        when(mockRel.getStartNode()).thenReturn(fromNode);
        when(mockRel.getEndNode()).thenReturn(toNode);
        
        when(mockRel.getPropertyKeys()).thenReturn(Collections.<String> emptyList());
        
        RelationshipSerializationStrategy strategy = new RelationshipSerializationStrategy();
        strategy.serialize(mockRel, serializer);
        serializer.flush();
        
        assertThat(new String(buffer.toByteArray()),is("{" +
                "\"self\":\"/db/data/relationship/0\"," +
                "\"type\":\"LOVES\"," +
                "\"start\":\"/db/data/node/0\"," +
                "\"end\":\"/db/data/node/1\"," +
                "\"data\":{}," +
                "\"extensions\":{}" +
            "}"));
    }
    
    @Test
    public void shouldSerializeNodeWithProperties() {
        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonSerializer serializer = new JsonSerializer(new JsonFactory(), buffer);
        
        Node fromNode = mock(Node.class);
        when(fromNode.getId()).thenReturn(0l);
        
        Node toNode = mock(Node.class);
        when(toNode.getId()).thenReturn(1l);
        
        Relationship mockRel = mock(Relationship.class);
        when(mockRel.getId()).thenReturn(0l);
        when(mockRel.getType()).thenReturn(DynamicRelationshipType.withName("LOVES"));
        when(mockRel.getStartNode()).thenReturn(fromNode);
        when(mockRel.getEndNode()).thenReturn(toNode);
        
        when(mockRel.getPropertyKeys()).thenReturn(new ArrayList<String>() {
            private static final long serialVersionUID = 1248180220593789023L;
        {
            add("name");
            add("age");
            add("favorite_numbers");
        }});
        
        when(mockRel.getProperty("name")).thenReturn("bob");
        when(mockRel.getProperty("age")).thenReturn(12);
        when(mockRel.getProperty("favorite_numbers")).thenReturn(new int [] {1,2,3});
        
        RelationshipSerializationStrategy strategy = new RelationshipSerializationStrategy();
        strategy.serialize(mockRel, serializer);
        serializer.flush();
        
        assertThat(new String(buffer.toByteArray()),is(
                "{" +
                  "\"self\":\"/db/data/relationship/0\"," +
                  "\"type\":\"LOVES\"," +
                  "\"start\":\"/db/data/node/0\"," +
                  "\"end\":\"/db/data/node/1\"," +
                  "\"data\":{" +
                    "\"name\":\"bob\"," +
                    "\"age\":12," +
                    "\"favorite_numbers\":[1,2,3]" +
                  "}," +
                  "\"extensions\":{}" +
                "}"));
    }

}
