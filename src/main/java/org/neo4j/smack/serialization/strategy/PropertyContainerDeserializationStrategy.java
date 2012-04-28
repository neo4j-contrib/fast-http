package org.neo4j.smack.serialization.strategy;

import java.util.Collections;
import java.util.Map;

import org.neo4j.smack.serialization.DeserializationException;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.Deserializer;

public class PropertyContainerDeserializationStrategy implements DeserializationStrategy<PropertyContainerDeserialization> 
{
    // TODO: Set up object pooling for PropertyContainerDeserialization
    @Override
    public PropertyContainerDeserialization deserialize(Deserializer in) throws DeserializationException
    {
        PropertyContainerDeserialization deserialized = new PropertyContainerDeserialization();
        
        Map<String,Object> properties;
        try {
            properties = in.readMap();
        } catch(DeserializationException e) {
            properties = Collections.<String,Object>emptyMap();
        }

        deserialized.setProperties(properties.entrySet().iterator());
        return deserialized;
    }

}
