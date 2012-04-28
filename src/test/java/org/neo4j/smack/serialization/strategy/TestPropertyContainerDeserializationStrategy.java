package org.neo4j.smack.serialization.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.Test;


public class TestPropertyContainerDeserializationStrategy extends SerializationStrategyTestBase {

    @Test
    public void shouldDeserializeEmptyMap() throws UnsupportedEncodingException 
    {   
        byte[] message = ("{}").getBytes("UTF-8");
        
        PropertyContainerDeserialization deserialized = deserialize(message, new PropertyContainerDeserializationStrategy());

        assertThat(deserialized.hasMoreProperties(), is(false));
    }
    
    @Test
    public void shouldDeserializePrimitiveValues() throws UnsupportedEncodingException 
    {   
        byte[] message = ("{" +
            "\"name\":\"Steven\"," +
            "\"age\":12," +
            "\"human\":true," +
            "\"height\":4.5" +
        "}").getBytes("UTF-8");
        
        PropertyContainerDeserialization deserialized = deserialize(message, new PropertyContainerDeserializationStrategy());

        assertThat(deserialized.hasMoreProperties(), is(true));
        deserialized.nextProperty();
        
        assertThat(deserialized.propertyKey(), is("name"));
        assertThat((String)deserialized.propertyValue(), is("Steven"));
        assertThat(deserialized.hasMoreProperties(), is(true));
        deserialized.nextProperty();
        
        assertThat(deserialized.propertyKey(), is("age"));
        assertThat((Integer)deserialized.propertyValue(), is(12));
        assertThat(deserialized.hasMoreProperties(), is(true));
        deserialized.nextProperty();
        
        assertThat(deserialized.propertyKey(), is("human"));
        assertThat((Boolean)deserialized.propertyValue(), is(true));
        assertThat(deserialized.hasMoreProperties(), is(true));
        deserialized.nextProperty();
        
        assertThat(deserialized.propertyKey(), is("height"));
        assertThat((Double)deserialized.propertyValue(), is(4.5d));
        
        assertThat(deserialized.hasMoreProperties(), is(false));
    }

}
