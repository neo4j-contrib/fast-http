package org.neo4j.smack.serialization.strategy;

import java.util.Collections;
import java.util.Map;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.smack.routing.UrlReverseLookerUpper;
import org.neo4j.smack.serialization.DeserializationStrategy;
import org.neo4j.smack.serialization.Deserializer;

/**
 * Deserializes a relationship
 */
public class RelationshipCreationDeserializationStrategy implements
        DeserializationStrategy<RelationshipCreationDescription> {
    
    private static final String TYPE_KEY = "type";
    private static final String TO_KEY = "to";
    private static final String DATA_KEY = "data";

    // TODO: Use some object pooling pattern to re-use RelationshipCreationDescription instances
    @Override
    public RelationshipCreationDescription deserialize(Deserializer in)
    {
        RelationshipCreationDescription desc = new RelationshipCreationDescription();
        Map<String, Object> raw = in.readMap();
        
        desc.setType(DynamicRelationshipType.withName((String) raw.get(TYPE_KEY)));
        desc.setEndNodeId(UrlReverseLookerUpper.nodeId((String)raw.get(TO_KEY)));
        
        @SuppressWarnings("unchecked")
        Map<String,Object> properties = raw.containsKey(DATA_KEY) ? (Map<String,Object>)raw.get(DATA_KEY) : Collections.<String,Object>emptyMap();
        desc.setProperties(properties.entrySet().iterator());
        
        return desc;
    }

}
