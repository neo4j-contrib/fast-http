package org.neo4j.smack.serialization.strategy;

import org.neo4j.smack.serialization.DeserializationException;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.Deserializer;

// TODO: Once we've added support for entering strings
// and arrays into the database in a streaming manner,
// extend this to allow exploiting that capability.
public class PropertyValueDeserializationStrategy implements DeserializationStrategy<Object> 
{

    @Override
    public Object deserialize(Deserializer in) throws DeserializationException
    {
        return in.readObject();
    }

}
