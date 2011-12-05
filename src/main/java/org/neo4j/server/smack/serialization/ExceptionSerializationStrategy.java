package org.neo4j.server.smack.serialization;

import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.SerializationModifier;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author mh
 * @since 27.11.11
 */
public class ExceptionSerializationStrategy implements SerializationStrategy<Throwable> {

    @Override
    public void serialize(Throwable error, Serializer out, SerializationModifier modifier) throws SerializationException {
        final StringWriter writer = new StringWriter();
        error.printStackTrace(new PrintWriter(writer));
        out.putString(writer.toString());
    }
}
