package org.neo4j.smack.routing;

import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Output;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.SerializationStrategy;

public class NotFoundEndpoint implements Endpoint {

    @Override
    public void invoke(Invocation invocation, Output result)
            throws Exception
    {
        result.notFound();
    }

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
