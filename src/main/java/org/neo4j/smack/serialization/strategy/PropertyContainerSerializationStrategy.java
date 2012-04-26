package org.neo4j.smack.serialization.strategy;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

/**
 * Serializes a property container into a map.
 */
public class PropertyContainerSerializationStrategy implements
        SerializationStrategy<PropertyContainer> {

    private static final PropertyValueSerializationStrategy valueSerialization = new PropertyValueSerializationStrategy();
    
    @Override
    public void serialize(PropertyContainer entity, Serializer out)
            throws SerializationException
    {
        out.startMap();
        for(String name : entity.getPropertyKeys()) {
            // TODO: Look into caching serialized property keys
            out.putMapPropertyName(name);
            valueSerialization.serialize(entity.getProperty(name), out);
        }
        out.endMap();
    }

}
