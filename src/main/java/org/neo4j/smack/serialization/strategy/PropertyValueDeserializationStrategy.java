package org.neo4j.smack.serialization.strategy;

import org.neo4j.smack.serialization.DeserializationException;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.Deserializer;

public class PropertyValueDeserializationStrategy implements DeserializationStrategy<Object> 
{

    @Override
    public Object deserialize(Deserializer in) throws DeserializationException
    {
        return in.readObject();
    }

}
