package org.neo4j.smack;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mh
 * @since 12.12.11
 */
public class REST {
    private final static JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    static Client client = Client.create();
    static String URI = RestServiceTest.BASE_URI;
    private WebResource.Builder resource;
    private int status;
    private URI location;
    private Object entity;
    private static String baseUri;
    private static String context;
    private final String path;
    private static GraphDatabaseService gds;

    public static void init(String baseUri, String context, GraphDatabaseService gds) {
        REST.baseUri = baseUri;
        REST.context = context;
        REST.URI = mergeUri(baseUri, context);
        REST.gds = gds;
    }

    REST(String path) {
        this.path = path;
        final String uri = mergeUri(URI, path);
        resource = client
                .resource(uri)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private static String mergeUri(String base, String path) {
        return (base + "/" + path).replaceAll("([^:]/)/", "$1");
    }

    public static REST from(String path) {
        return new REST(path);
    }

    public static REST to(String path) {
        return new REST(path);
    }

    public REST get() {
        return handle(resource.get(ClientResponse.class));
    }

    public REST post(Object data) {
        return handle(resource.post(ClientResponse.class, formatJson(data)));
    }

    private REST handle(ClientResponse response) {
        status = response.getStatus();
        location = response.getLocation();
        readEntityIfPossible(response);
        response.close();
        System.out.printf("GET from [%s], status code [%d] location: %s, returned data: %n%s", path, status, location, entity);
        return this;
    }

    private void readEntityIfPossible(ClientResponse response) {
        if (status != Response.Status.NO_CONTENT.getStatusCode()) {
            this.entity = readEntity(response, Object.class);
        }
    }

    private <T> T readEntity(ClientResponse response, Class<T> type) {
        try {
            final InputStream is = response.getEntityInputStream();
            return jsonFactory.createJsonParser(new FilterInputStream(is) {
                @Override
                public int read() throws IOException {
                    final int result = super.read();
                    System.out.print(Character.valueOf((char) result));
                    return result;
                }
            }).readValueAs(type);
        } catch (Exception e) {
            System.out.flush();
            throw new RuntimeException("Error reading response object", e);
        }
    }


    private String formatJson(Object data) {
        try {
            final StringWriter result = new StringWriter();
            jsonFactory.createJsonGenerator(result).writeObject(data);
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException("error formatting json", e);
        }
    }

    public REST put(Object data) {
        return handle(resource.put(ClientResponse.class, formatJson(data)));
    }

    public REST delete() {
        return handle(resource.delete(ClientResponse.class));
    }

    public REST created() {
        return assertStatus(Response.Status.CREATED);
    }

    public REST ok() {
        return assertStatus(Response.Status.OK);
    }

    public REST location(String location) {
        assertTrue("Location", mergeUri(URI, location).matches(this.location.toString())); // todo full uri vs path
        return this;
    }

    public REST expect(Object value) {
        assertEquals("Result ", value, this.entity);
        return this;
    }

    public REST expect(String path, Object value) {
        assertEquals("Result[" + path + "]", value, getPath(path));
        return this;
    }

    public REST expectUri(String path, Object value) {
        assertEquals("Result[" + path + "]", mergeUri(URI, value.toString()), getPath(path));
        return this;
    }

    private Object getPath(String path) {
        final String[] parts = path.split("[./]");
        Map map = (Map) entity;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            map = (Map) map.get(part);
        }
        return map.get(parts[parts.length - 1]);
    }

    public REST assertStatus(Response.Status expectedStatus) {
        assertEquals("status code is not " + expectedStatus, expectedStatus.getStatusCode(), status);
        return this;
    }

    public void checkNode(String...props) {
        checkPropertyContainer(node(), props);
    }

    private void checkPropertyContainer(PropertyContainer container, String[] props) {
        Map data = data();
        for (String prop : props) {
            assertEquals(container.getProperty(prop), data.get(prop));
        }
    }

    public void checkRelationship(String...props) {
        checkPropertyContainer(relationship(),props);
    }

    private Map data() {
        return (Map) ((Map) entity).get("data");
    }

    private Node node() {
        return gds.getNodeById(id());
    }

    private Long id() {
        if (location!=null) {
            return extractId(location.toString());
        }
        return extractId(selfUri());
    }

    private Relationship relationship() {
        return gds.getRelationshipById(id());
    }

    private String selfUri() {
        return (String) ((Map)entity).get("self");
    }

    private Long extractId(String uri) {
        final String[] parts = uri.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }
}
