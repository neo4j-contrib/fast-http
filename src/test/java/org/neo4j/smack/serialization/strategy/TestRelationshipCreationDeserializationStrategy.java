package org.neo4j.smack.serialization.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.Test;


public class TestRelationshipCreationDeserializationStrategy extends SerializationStrategyTestBase {

    @Test
    public void shouldDeserializeOnlyTypeAndTo() throws UnsupportedEncodingException 
    {   
        byte[] message = ("{" +
            "\"to\":\"/db/data/node/44\"," +
            "\"type\":\"LOVES\"" +
        "}").getBytes("UTF-8");
        
        RelationshipCreationDescription deserialized = deserialize(message, new RelationshipCreationDeserializationStrategy());
        
        assertThat(deserialized.getType().name(), is("LOVES"));
        assertThat(deserialized.getEndNodeId(), is(44l));

        assertThat(deserialized.hasMoreProperties(), is(false));
    }
    
    @Test
    public void shouldDeserializeTypeToAndData() throws UnsupportedEncodingException 
    {   
        byte[] message = ("{" +
            "\"to\":\"/db/data/node/44\"," +
            "\"type\":\"LOVES\"," +
            "\"data\": {" +
                "\"name\":\"bob\"," +
                "\"age\":12" +
            "}" +
        "}").getBytes("UTF-8");
        
        RelationshipCreationDescription deserialized = deserialize(message, new RelationshipCreationDeserializationStrategy());
        
        assertThat(deserialized.getType().name(), is("LOVES"));
        assertThat(deserialized.getEndNodeId(), is(44l));
        
        deserialized.nextProperty();
        assertThat(deserialized.propertyKey(), is("name"));
        assertThat((String)deserialized.propertyValue(), is("bob"));
        
        assertThat(deserialized.hasMoreProperties(), is(true));
        
        deserialized.nextProperty();
        assertThat(deserialized.propertyKey(), is("age"));
        assertThat((Integer)deserialized.propertyValue(), is(12));
        
        assertThat(deserialized.hasMoreProperties(), is(false));
    }

}
