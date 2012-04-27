package org.neo4j.smack.serialization.strategy;

import java.util.Collections;
import java.util.Map;

import org.neo4j.smack.serialization.DeserializationException;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.Deserializer;

public class PropertyContainerDeserializationStrategy implements DeserializationStrategy<Map<String,Object>> 
{
    @Override
    public Map<String,Object> deserialize(Deserializer in) throws DeserializationException
    {
        try {
            return in.readMap();
        } catch(DeserializationException e) {
            return Collections.<String,Object>emptyMap();
        }
    }

}
