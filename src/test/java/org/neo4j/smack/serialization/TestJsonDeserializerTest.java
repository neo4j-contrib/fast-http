package org.neo4j.smack.serialization;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.neo4j.smack.serialization.JsonDeserializer;

public class TestJsonDeserializerTest {

    @Test
    public void testReadingMap() throws Exception {
        InputStream in = new ByteArrayInputStream("{\"firstkey\":1,\"secondkey\":2}".getBytes("UTF-8"));
        
        JsonDeserializer d = new JsonDeserializer(new JsonFactory(new ObjectMapper()), in);
        
        Map<String,Object> deserialized = d.readMap();
        
        assertThat(deserialized.containsKey("firstkey"), is(true));
        assertThat(deserialized.containsKey("secondkey"), is(true));
        
        assertThat((Integer)deserialized.get("firstkey"), is(1));
        assertThat((Integer)deserialized.get("secondkey"), is(2));
    }
    
}
