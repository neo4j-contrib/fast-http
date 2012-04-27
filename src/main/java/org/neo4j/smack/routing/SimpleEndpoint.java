package org.neo4j.smack.routing;

import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.SerializationStrategy;

public abstract class SimpleEndpoint implements Endpoint {

    @Override
    public InvocationVerb getVerb()
    {
        return InvocationVerb.GET;
    }

    @Override
    public DeserializationStrategy<?> getDeserializationStrategy()
    {
        return DeserializationStrategy.NO_OP;
    }

    @Override
    public SerializationStrategy<?> getSerializationStrategy()
    {
        return SerializationStrategy.NO_OP;
    }

    @Override
    public boolean isTransactional()
    {
        return false;
    }

}
