package org.neo4j.server.smack.serialization;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @since 27.11.11
 */
public class GraphElementSerializer {
    public static Map<String, Object> toNodeMap(Node node) {
        final Map<String, Object> data = toMap(node);
        for (Relationship relationship : node.getRelationships()) {
            final String typeName = relationship.getType().name();
            final List<Long> list = getTypeList(data, typeName);
            list.add(relationship.getId());

        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private static List<Long> getTypeList(Map<String, Object> data, String typeName) {
        if (!data.containsKey(typeName)) {
            final List<Long> list = new ArrayList<Long>();
            data.put(typeName, list);
            return list;
        } else {
            return ((List<Long>)data.get(typeName));
        }
    }
    public static Map<String, Object> toRelationshipMap(Relationship rel) {
        Map<String,Object> data = toMap(rel);
        data.put("start",rel.getStartNode().getId());
        data.put("end", rel.getEndNode().getId());
        data.put("type", rel.getType().name());
        return data;
    }

    private static Map<String, Object> toMap(PropertyContainer pc) {
        Map<String, Object> result=new HashMap<String, Object>();
        for (String prop : pc.getPropertyKeys()) {
            result.put(prop, pc.getProperty(prop));
        }
        return result;
    }
}
