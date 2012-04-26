package org.neo4j.smack.serialization;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.jackson.JsonFactory;
import org.junit.Test;

public class TestSerializationStrategy {

    @Test
    public void testSimpleSerialization() throws Exception 
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        JsonSerializer serializer = new JsonSerializer(new JsonFactory(), buffer);
        
        SerializationStrategy<Object> simpleStrategy = new SerializationStrategy<Object>() {
            @Override
            public void serialize(Object value, Serializer out)
                    throws SerializationException
            {
                out.startList();
                out.putString("Hello!");
                out.endList();
                out.flush();
            }
        };
        
        simpleStrategy.serialize(null, serializer);
        
        assertThat(new String(buffer.toByteArray()),is("[\"Hello!\"]"));
        
    }
    
}
