package org.neo4j.smack.test.util;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.smack.Neo4jServer;
import org.neo4j.smack.Smack;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;

public class ServerHelper {

    public static final String HOST = "localhost";
    public static final int PORT = 7473;

    public static Neo4jServer createServer() {
        return new Neo4jServer(HOST, PORT, new ImpermanentGraphDatabase());
    }
    
    public static void cleanTheDatabase(final Neo4jServer server) {
        new Transactor( server.getSmackServer().getDatabase(), new UnitOfWork()
        {
            @Override
            public void doWork()
            {
                deleteAllNodesAndRelationships( server.getSmackServer() );

                deleteAllIndexes( server.getSmackServer() );
            }

            private void deleteAllNodesAndRelationships( final Smack server )
            {
                Iterable<Node> allNodes = GlobalGraphOperations.at( server.getDatabase() ).getAllNodes();
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

            private void deleteAllIndexes( final Smack server )
            {
                for ( String indexName : server.getDatabase().index()
                        .nodeIndexNames() )
                {
                    try{
                        server.getDatabase().index()
                                .forNodes( indexName )
                                .delete();
                    } catch(UnsupportedOperationException e) {
                        // Encountered a read-only index.
                    }
                }

                for ( String indexName : server.getDatabase().index()
                        .relationshipIndexNames() )
                {
                    try {
                        server.getDatabase().index()
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
