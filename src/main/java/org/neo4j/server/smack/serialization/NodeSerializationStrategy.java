package org.neo4j.server.smack.serialization;

import org.neo4j.graphdb.Node;

/**
 * @author mh
 * @since 27.11.11
 */
public class NodeSerializationStrategy implements SerializationStrategy<Node> {

    @Override
    public void serialize(Node node, Serializer out, SerializationModifier modifier) throws SerializationException {
        out.putNode(node);
    }
}
