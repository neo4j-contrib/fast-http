package org.neo4j.smack;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.smack.api.DataAPI;
import org.neo4j.test.ImpermanentGraphDatabase;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 14.11.11
 */
public class RestServiceTest {
    public static final String HOST = "localhost";
    public static final int PORT = 7473;
    public static final String BASE_URI = "http://" + HOST + ":" + PORT + "/";
    public static final int ROOT_NODE_ID = 0;
    static SmackServer server;
    private static AbstractGraphDatabase gds;
    private final static JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    @BeforeClass
    public static void setUp() throws Exception {
        gds = new ImpermanentGraphDatabase();
        final Transaction tx = gds.beginTx();
        gds.getReferenceNode().setProperty("name", "test");
        tx.success();
        tx.finish();
        server = new SmackServer(HOST, PORT, new Database(gds));
        server.addRoute("", new DataAPI());
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
        gds.shutdown();
    }

    @Test
    public void testGetDbInfo() throws Exception {
        final String uri = BASE_URI + "db/data";
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        final int status = response.getStatus();
        final Map content = readEntity(response, Map.class);
        System.out.printf("GET to [%s], status code [%d], returned data: %n%s",
                uri, status, content);
        response.close();
        assertEquals("info returned 200", 200, status);
        assertEquals("info reference node",BASE_URI+"db/data/node/0",content.get("reference_node"));
    }

    @Test
    public void testCreateNode() throws Exception {
        final String uri = BASE_URI + "db/data/node";
        WebResource resource = Client.create().resource(uri);
        String data = "{\"name\":\"john\",\"age\":10}";
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(data)
                .post(ClientResponse.class);

        final int status = response.getStatus();
        final URI location = response.getLocation();
        System.out.println("response.getEntityTag() = " + response.getEntityTag());
        final Map entity = readEntity(response, Map.class);
        System.out.printf("POST [%s] to [%s], status code [%d] location: %s, returned data: %n%s",
                data, uri, status, location, entity);
        response.close();
        assertEquals("info returned 201", 201, status);
        assertEquals("info returned location for new node", "/db/data/node/1", location.toString());
        for (Node node : gds.getAllNodes()) {
            System.out.println("node = " + node);
            for (String prop : node.getPropertyKeys()) {
                System.out.println(prop + ": " + node.getProperty(prop));
            }
        }
        assertEquals("john", gds.getNodeById(1).getProperty("name"));
        assertEquals(10, gds.getNodeById(1).getProperty("age"));
    }

    private <T> T readEntity(ClientResponse response, Class<T> type) throws IOException {
        final InputStream is = response.getEntityInputStream();
        return jsonFactory.createJsonParser(is).readValueAs(type);
    }

    @Test
    public void testGetNode() throws Exception {
        final String uri = BASE_URI + "db/data/node/"+ROOT_NODE_ID;
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        final int status = response.getStatus();
        final URI location = response.getLocation();
        final Map result = readEntity(response, Map.class);
        System.out.printf("GET from [%s], status code [%d] location: %s, returned data: %n%s",
                uri, status, location, result);
        response.close();
        assertEquals("get-node returned 200", 200, status);
        assertEquals("get-node returned location for new node", BASE_URI+"db/data/node/"+ROOT_NODE_ID, result.get("self"));
    }
}
