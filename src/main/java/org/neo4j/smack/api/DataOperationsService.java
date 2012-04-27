package org.neo4j.smack.api;

import org.neo4j.smack.routing.RoutingDefinition;

public class DataOperationsService extends RoutingDefinition {

    public DataOperationsService(String dataAPIPath) {
        addRoute("", new NodeService());
        addRoute("", new RelationshipService(dataAPIPath));
        addRoute("", new IndexService(dataAPIPath));
        addRoute("", new TraversalService(dataAPIPath));
        addRoute("", new DataOperationsDiscoveryService());
    }

}
