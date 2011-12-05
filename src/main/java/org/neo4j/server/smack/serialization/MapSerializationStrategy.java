package org.neo4j.server.smack.serialization;

import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.SerializationModifier;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

import java.util.Map;

/**
 * @author mh
 * @since 27.11.11
 */
public class MapSerializationStrategy implements SerializationStrategy<Map> {

    @Override
    public void serialize(Map map, Serializer out, SerializationModifier modifier) throws SerializationException {
        out.putMap(map);
    }
}
