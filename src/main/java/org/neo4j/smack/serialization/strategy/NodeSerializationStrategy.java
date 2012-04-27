package org.neo4j.smack.serialization.strategy;

import org.neo4j.graphdb.Node;
import org.neo4j.smack.routing.UrlReverseLookerUpper;
import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

public class NodeSerializationStrategy implements SerializationStrategy<Node> {

    private static final String SELF_FIELD = "self";
    private static final String DATA_FIELD = "data";
    private static final String EXTENSIONS_FIELD = "extensions";
    
    private static final UrlReverseLookerUpper url = new UrlReverseLookerUpper();
    private static final PropertyContainerSerializationStrategy propertySerialization = new PropertyContainerSerializationStrategy();
    
    @Override
    public void serialize(Node node, Serializer out) throws SerializationException {
        out.startMap();
        
        out.putMapPropertyName(DATA_FIELD);
        propertySerialization.serialize(node, out);
        
        out.putMapPropertyName(SELF_FIELD);
        out.putString(url.reverse(node));
        
        out.putMapPropertyName(EXTENSIONS_FIELD);
        out.startMap();
        out.endMap();
        
        out.endMap();
    }
}
