package org.neo4j.smack.serialization.strategy;

import java.util.Map;

import org.neo4j.smack.serialization.AbstractNonStreamingSerializationStrategy;
import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.Serializer;

/**
 * @author mh
 * @since 27.11.11
 */
public class MapSerializationStrategy extends AbstractNonStreamingSerializationStrategy<Map<String,Object>> {

    @Override
    public void serialize(Map<String,Object> map, Serializer out) throws SerializationException {
        out.putMap(map);
    }
}
