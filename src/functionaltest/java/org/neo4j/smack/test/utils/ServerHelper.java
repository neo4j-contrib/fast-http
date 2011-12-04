package org.neo4j.smack.test.utils;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.smack.Database;
import org.neo4j.smack.SmackServer;
import org.neo4j.smack.api.DataAPI;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;

public class ServerHelper {

    public static SmackServer createServer() {
        SmackServer server = new SmackServer("localhost", 7474, new Database(new ImpermanentGraphDatabase()));
        server.addRoute("",new DataAPI());
        return server;
    }
    
    public static void cleanTheDatabase(final SmackServer server) {
        new Transactor( server.getDatabase().graph, new UnitOfWork()
        {
            @Override
            public void doWork()
            {
                deleteAllNodesAndRelationships( server );

                deleteAllIndexes( server );
            }

            private void deleteAllNodesAndRelationships( final SmackServer server )
            {
                Iterable<Node> allNodes = GlobalGraphOperations.at( server.getDatabase().graph ).getAllNodes();
                for ( Node n : allNodes )
                {
                    Iterable<Relationship> relationships = n.getRelationships();
                    for ( Relationship rel : relationships )
                    {
                        rel.delete();
                    }
                    if ( n.getId() != 0 )
                    { // Don't delete the reference node - tests depend on it
                      // :-(
                        n.delete();
                    }
                    else
                    { // Remove all state from the reference node instead
                        for ( String key : n.getPropertyKeys() )
                        {
                            n.removeProperty( key );
                        }
                    }
                }
            }

            private void deleteAllIndexes( final SmackServer server )
            {
                for ( String indexName : server.getDatabase().graph.index()
                        .nodeIndexNames() )
                {
                    try{
                        server.getDatabase().graph.index()
                                .forNodes( indexName )
                                .delete();
                    } catch(UnsupportedOperationException e) {
                        // Encountered a read-only index.
                    }
                }

                for ( String indexName : server.getDatabase().graph.index()
                        .relationshipIndexNames() )
                {
                    try {
                        server.getDatabase().graph.index()
                                .forRelationships( indexName )
                                .delete();
                    } catch(UnsupportedOperationException e) {
                        // Encountered a read-only index.
                    }
                }
            }
        } ).execute();
    }
}
