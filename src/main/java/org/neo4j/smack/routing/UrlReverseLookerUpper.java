package org.neo4j.smack.routing;

import org.neo4j.graphdb.Node;

/**
 * Central authority for reverse url lookups.
 * Dead simple right now, but to be expanded down the road.
 */
public class UrlReverseLookerUpper {

    public String reverse(Node node)
    {
        return "/db/data/node/" + node.getId();
    }

    public String reverseTransaction(Long txId)
    {
        return "/db/data/tx/" + txId;
    }
    
}
