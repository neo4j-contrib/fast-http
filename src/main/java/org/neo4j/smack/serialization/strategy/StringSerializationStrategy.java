package org.neo4j.smack.serialization.strategy;

import org.neo4j.smack.serialization.AbstractNonStreamingSerializationStrategy;
import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.Serializer;

public class StringSerializationStrategy extends AbstractNonStreamingSerializationStrategy<String> {

    @Override
    public void serialize(String value, Serializer out) throws SerializationException
    {
        out.putString(value);
    }
}
