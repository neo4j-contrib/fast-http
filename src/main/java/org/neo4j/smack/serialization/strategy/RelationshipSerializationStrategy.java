package org.neo4j.smack.serialization.strategy;

import org.neo4j.graphdb.Relationship;
import org.neo4j.smack.routing.UrlReverseLookerUpper;
import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

public class RelationshipSerializationStrategy implements SerializationStrategy<Relationship> {

    private static final String END_FIELD = "end";
    private static final String START_FIELD = "start";
    private static final String TYPE_FIELD = "type";
    private static final String SELF_FIELD = "self";
    private static final String DATA_FIELD = "data";
    private static final String EXTENSIONS_FIELD = "extensions";
    
    private static final UrlReverseLookerUpper url = new UrlReverseLookerUpper();
    private static final PropertyContainerSerializationStrategy propertySerialization = new PropertyContainerSerializationStrategy();
    
    @Override
    public void serialize(Relationship rel, Serializer out) throws SerializationException {
        out.startMap();

        out.putMapPropertyName(SELF_FIELD);
        out.putString(url.reverse(rel));

        out.putMapPropertyName(TYPE_FIELD);
        out.putString(rel.getType().name());

        out.putMapPropertyName(START_FIELD);
        out.putString(url.reverse(rel.getStartNode()));

        out.putMapPropertyName(END_FIELD);
        out.putString(url.reverse(rel.getEndNode()));
        
        out.putMapPropertyName(DATA_FIELD);
        propertySerialization.serialize(rel, out);
        
        out.putMapPropertyName(EXTENSIONS_FIELD);
        out.startMap();
        out.endMap();
        
        out.endMap();
    }

}
