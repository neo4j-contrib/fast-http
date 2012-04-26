package org.neo4j.smack.serialization;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class TestDeserializationStrategy {

    @Test
    public void testSimpleDeserializationStrategy() throws Exception
    {
        InputStream in = new ByteArrayInputStream("{\"firstkey\":1,\"secondkey\":2}".getBytes("UTF-8"));
        JsonDeserializer d = new JsonDeserializer(new JsonFactory(new ObjectMapper()), in);
        
        DeserializationStrategy<Object> objectStrategy = new DeserializationStrategy<Object>() {
            @Override
            public Object deserialize(Deserializer in)
                    throws DeserializationException
            {
                return in.readObject();
            }
        };
        
        assertThat(objectStrategy.deserialize(d),not(nullValue()));
    }
    
}
