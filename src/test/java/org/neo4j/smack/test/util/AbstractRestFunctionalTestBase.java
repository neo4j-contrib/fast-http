package org.neo4j.smack.test.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonParseException;
import org.junit.Before;
import org.junit.Rule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.Pair;
import org.neo4j.test.GraphDescription;
import org.neo4j.test.GraphHolder;
import org.neo4j.test.TestData;
import org.neo4j.visualization.asciidoc.AsciidocHelper;

public class AbstractRestFunctionalTestBase extends SharedSmackServerTestBase implements GraphHolder {
    protected static final String NODES = "http://localhost:7473/db/data/node/";

    public @Rule
    TestData<Map<String, Node>> data = TestData.producedThrough( GraphDescription.createGraphFor(
            this, true ) );

    public @Rule
    TestData<RESTDocsGenerator> gen = TestData.producedThrough( RESTDocsGenerator.PRODUCER );

    protected String doCypherRestCall( String endpoint, String script, Status status, Pair<String, String>... params ) {
        data.get();
        String parameterString = createParameterString( params );


        String queryString = "{\"query\": \"" + createScript( script ) + "\"," + parameterString+"},"  ;

        gen.get().expectedStatus( status.getStatusCode() ).payload(
                queryString ).description(
                AsciidocHelper.createCypherSnippet( script ) );
        return gen.get().post( endpoint ).entity();
    }
    
    protected String doGremlinRestCall( String endpoint, String script, Status status, Pair<String, String>... params ) {
        data.get();
        String parameterString = createParameterString( params );


        String queryString = "{\"script\": \"" + createScript( script ) + "\"," + parameterString+"},"  ;

        gen.get().expectedStatus( status.getStatusCode() ).payload(
                queryString ).description(formatGroovy( createScript( script ) ) );
        return gen.get().post( endpoint ).entity();
    }
    
    protected String formatGroovy( String script )
    {
        script = script.replace( ";", "\n" );
        if ( !script.endsWith( "\n" ) )
        {
            script += "\n";
        }
        return "_Raw script source_\n\n" + "[source, groovy]\n" + "----\n"
               + script + "----\n";
    }
    
    private Long idFor( String name ) {
        return data.get().get( name ).getId();
    }
    
    private String createParameterString( Pair<String, String>[] params ) {
        String paramString = "\"params\": {";
        for( Pair<String, String> param : params ) {
            String delimiter = paramString.endsWith( "{" ) ? "" : ",";

            paramString += delimiter + "\"" + param.first() + "\":\"" + param.other() + "\"";
        }
        paramString += "}";

        return paramString;
    }

    protected String createScript( String template ) {
        for( String key : data.get().keySet() ) {
            template = template.replace( "%" + key + "%", idFor( key ).toString() );
        }
        return template;
    }
    
    protected String startGraph( String name )
    {
        return AsciidocHelper.createGraphViz( "Starting Graph", graphdb(), name);
    }

    @Override
    public GraphDatabaseService graphdb()
    {
        return server().getDatabase().graph;
    }

    @Before
    public void cleanContent()
    {
        cleanDatabase();
        gen.get().setGraph( graphdb() );
    }

    protected String getDataUri()
    {
        return "http://localhost:7473/db/data/";
    }
    
    protected String getBaseUri()
    {
        return "http://localhost:7473";
    }

    protected String getNodeUri( Node node )
    {
        return getDataUri() + "node/" + node.getId();
    }
    
    protected String getRelationshipUri( Relationship node )
    {
        return getDataUri() + "relationship/" + node.getId();
    }
    
    protected String getNodeIndexUri( String indexName, String key, String value )
    {
        return postNodeIndexUri( indexName ) + "/" + key + "/" + value;
    }
    
    protected String postNodeIndexUri( String indexName )
    {
        return getDataUri() + "index/node/" + indexName;
    }
    
    protected String postRelationshipIndexUri( String indexName )
    {
        return getDataUri() + "index/relationship/" + indexName;
    }

    protected String getRelationshipIndexUri( String indexName, String key, String value )
    {
        return getDataUri() + "index/relationship/" + indexName + "/" + key + "/" + value;
    }

    protected Node getNode( String name )
    {
        return data.get().get( name );
    }

    protected Node[] getNodes( String... names )
    {
        Node[] nodes = {};
        ArrayList<Node> result = new ArrayList<Node>();
        for (String name : names)
        {
            result.add( getNode( name ) );
        }
        return result.toArray(nodes);
    }
    
    public void assertSize(int expectedSize, String entity) throws JsonParseException, IOException {
        Collection<?> hits = (Collection<?>) JsonHelper.jsonToSingleValue( entity );
        assertEquals( expectedSize, hits.size() );
    }
    
    public String getPropertiesUri( Relationship rel )
    {
        return getRelationshipUri(rel)+  "/properties";
    }
    public String getPropertiesUri( Node node )
    {
        return getNodeUri(node)+  "/properties";
    }
    
    public RESTDocsGenerator gen() {
        return gen.get();
    }
    
    public void description( String description )
    {
        gen().description( description );
        
    }
}
