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
public class SmackServerTest {
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
        server.addRoute("", new DataAPI(HOST,PORT));
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
        gds.shutdown();
    }

    @Test
    public void testGetInfo() throws Exception {
        final String uri = BASE_URI + "info";
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        final int status = response.getStatus();
        System.out.printf("GET to [%s], status code [%d], returned data: %n%s",
                uri, status, response.getEntity(String.class));
        response.close();
        assertEquals("info returned 200", 200, status);
    }

    @Test
    public void testGetWrongUriReturnsError() throws Exception {
        final String uri = BASE_URI + "wrong_uri";
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        final int status = response.getStatus();
        System.out.printf("GET to [%s], status code [%d], returned data: %n%s",
                uri, status, response.getEntity(String.class));
        response.close();
        assertEquals("wrong uri returned 404", 404, status);
    }

    @Test
    public void testWrongMethodReturnsError() throws Exception {
        final String uri = BASE_URI + "wrong_uri";
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        final int status = response.getStatus();
        System.out.printf("POST to [%s], status code [%d], returned data: %n%s",
                uri, status, response.getEntity(String.class));
        response.close();
        assertEquals("wrong method returned 404", 404, status);
    }

    @Test
    public void testCreateNode() throws Exception {
        final String uri = BASE_URI + "node";
        WebResource resource = Client.create().resource(uri);
        String data = "{\"name\":\"john\",\"age\":10}";
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(data)
                .post(ClientResponse.class);

        final int status = response.getStatus();
        final URI location = response.getLocation();
        final String entity = response.getEntity(String.class);
        System.out.printf("POST [%s] to [%s], status code [%d] location: %s, returned data: %n%s",
                data, uri, status, location, entity);
        response.close();
        assertEquals("info returned 201", 201, status);
        assertEquals("info returned location for new node", "/node/1", location.toString());
        for (Node node : gds.getAllNodes()) {
            System.out.println("node = " + node);
            for (String prop : node.getPropertyKeys()) {
                System.out.println(prop + ": " + node.getProperty(prop));
            }
        }
        assertEquals("john", gds.getNodeById(1).getProperty("name"));
        assertEquals(10, gds.getNodeById(1).getProperty("age"));
    }

    @Test
    public void testWrongPayloadReturnsError() throws Exception {
        final String uri = BASE_URI + "node";
        WebResource resource = Client.create().resource(uri);
        String data = "123";
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(data)
                .post(ClientResponse.class);

        final int status = response.getStatus();
        System.out.printf("POST [%s] to [%s], status code [%d] location: %s, returned data: %n%s",
                data, uri, status, response.getLocation(), response.getEntity(String.class));
        response.close();
        assertEquals("info returned 500", 500, status);
    }

    @Test
    public void testGetNode() throws Exception {
        final String uri = BASE_URI + "node/" + ROOT_NODE_ID;
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        final int status = response.getStatus();
        final URI location = response.getLocation();
        final Map data = readEntity(response, Map.class);
        System.out.printf("GET from [%s], status code [%d] location: %s, returned data: %n%s",
                uri, status, location, data);
        response.close();
        assertEquals("info returned 201", 200, status);
        assertEquals("test", data.get("name"));
        //assertEquals("info returned location for new node", "/node/0", location.toString());
    }

    private <T> T parseJson(String entity, Class<T> type) throws IOException {
        return jsonFactory.createJsonParser(entity).readValueAs(type);
    }

    private <T> T readEntity(ClientResponse response, Class<T> type) throws IOException {
        final InputStream is = response.getEntityInputStream();
        return jsonFactory.createJsonParser(is).readValueAs(type);
    }
}
