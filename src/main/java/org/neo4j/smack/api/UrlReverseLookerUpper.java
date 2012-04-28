package org.neo4j.smack.api;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Central authority for url lookups.
 * Dead simple right now, but to be expanded down the road.
 * 
 * TODO: Perhaps move path constants into their own class?
 */
public class UrlReverseLookerUpper {

    //
    // Path variable keys
    //
    

    public static final String PROPERTY_KEY_NAME    = "key";
    
    public static final String NODE_ID_NAME         = "node_id";
    
    public static final String RELATIONSHIP_ID_NAME        = "relationship_id";
    public static final String RELATIONSHIP_DIRECTION_NAME = "direction";
    public static final String RELATIONSHIP_TYPES_NAME     = "types";
    
    public static final String PATH_NODES           = "node";
    public static final String PATH_NODE            = PATH_NODES + "/{" + NODE_ID_NAME + "}";
    public static final String PATH_NODE_PROPERTIES = PATH_NODE + "/properties";
    public static final String PATH_NODE_PROPERTY   = PATH_NODE_PROPERTIES + "/{" + PROPERTY_KEY_NAME + "}";
    
    //
    // Paths
    //
    
    public static final String PATH_NODE_RELATIONSHIPS = PATH_NODE + "/relationships";
    public static final String PATH_RELATIONSHIP = "relationship/{" + RELATIONSHIP_ID_NAME + "}";
    
    public static final String PATH_NODE_RELATIONSHIPS_W_DIR = PATH_NODE_RELATIONSHIPS + "/{"+RELATIONSHIP_DIRECTION_NAME+"}";
    public static final String PATH_NODE_RELATIONSHIPS_W_DIR_N_TYPES = PATH_NODE_RELATIONSHIPS_W_DIR + "/{"+RELATIONSHIP_TYPES_NAME+"}";
    public static final String PATH_RELATIONSHIP_PROPERTIES = PATH_RELATIONSHIP + "/properties";
    public static final String PATH_RELATIONSHIP_PROPERTY = PATH_RELATIONSHIP_PROPERTIES + "/{"+PROPERTY_KEY_NAME+"}";
    
    public String reverse(Node node)
    {
        return "/db/data/node/" + node.getId();
    }

    public String reverse(Relationship rel)
    {
        return "/db/data/relationship/" + rel.getId();
    }

    public String reverseTransaction(Long txId)
    {
        return "/db/data/tx/" + txId;
    }

    // TODO: This is used by deserializers,
    // see if we can write a special deserializer
    // that extracts this id without creating garbage.
    public static long nodeId(String string)
    {
        String [] parts = string.split("\\/");
        return Long.valueOf(parts[parts.length-1]);
    }
    
}
