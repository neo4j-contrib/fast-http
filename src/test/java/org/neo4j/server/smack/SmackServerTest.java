package org.neo4j.server.smack;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.server.database.Database;
import org.neo4j.server.rest.service.DataAPI;
import org.neo4j.server.rest.service.DatabaseWorkerPool;
import org.neo4j.test.ImpermanentGraphDatabase;

import javax.ws.rs.core.MediaType;
import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 14.11.11
 */
public class SmackServerTest {
    public static final String HOST = "localhost";
    public static final int PORT = 7476;
    public static final String BASE_URI = "http://" + HOST + ":" + PORT + "/";
    public static final int ROOT_NODE_ID = 0;
    static SmackServer server;
    private static ImpermanentGraphDatabase gds;

    @BeforeClass
    public static void setUp() throws Exception {
        gds = new ImpermanentGraphDatabase();
        server = new SmackServer(HOST, PORT,new DatabaseWorkerPool(new Database(gds)));
        server.addRoute("",new DataAPI());
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
        gds.shutdown();
    }

    @Test
    public void testGetInfo() throws Exception {
        final String uri = BASE_URI+ "info";
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        final int status = response.getStatus();
        System.out.printf("GET to [%s], status code [%d], returned data: %n%s",
                uri, status, response.getEntity(String.class));
        response.close();
        assertEquals("info returned 200",200,status);
    }

    @Test
    public void testCreateNode() throws Exception {
        final String uri = BASE_URI+"node";
        WebResource resource = Client.create().resource(uri);
        String data = "{\"name\":\"john\",\"age\":10}";
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( data )
                .post( ClientResponse.class );

        final int status = response.getStatus();
        final URI location = response.getLocation();
        final String entity = response.getEntity(String.class);
        System.out.printf("POST [%s] to [%s], status code [%d] location: %s, returned data: %n%s",
                data, uri, status, location, entity);
        response.close();
        assertEquals("info returned 201", 201, status);
        assertEquals("info returned location for new node", "/node/1", location.toString());
        assertEquals("john",gds.getNodeById(1).getProperty("name"));
        assertEquals(10,gds.getNodeById(1).getProperty("age"));
    }
    @Test
    @Ignore
    public void testGetNode() throws Exception {
        final String uri = BASE_URI+"node/"+ ROOT_NODE_ID;
        WebResource resource = Client.create().resource(uri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);

        final int status = response.getStatus();
        final URI location = response.getLocation();
        final String entity = response.getEntity(String.class);
        System.out.printf("GET from [%s], status code [%d] location: %s, returned data: %n%s",
                 uri, status, location, entity);
        response.close();
        assertEquals("info returned 201", 200, status);
        assertEquals("info returned location for new node", "/node/0", location.toString());
    }
}
