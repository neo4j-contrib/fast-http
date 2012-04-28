package org.neo4j.smack.serialization.strategy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.JsonDeserializer;

public class SerializationStrategyTestBase {

    JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    
    public <T> T deserialize(byte[] bytes,
            DeserializationStrategy<T> strategy)
    {
        InputStream in = new ByteArrayInputStream(bytes);
        JsonDeserializer deserializer = new JsonDeserializer(jsonFactory, in);
        return strategy.deserialize(deserializer);
    }
}
