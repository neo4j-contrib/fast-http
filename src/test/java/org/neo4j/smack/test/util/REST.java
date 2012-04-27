package org.neo4j.smack.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @author mh
 * @since 12.12.11
 */
public class REST {
    private final static JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    
    private final static Client client = Client.create();
    
    private String baseUri;
    private GraphDatabaseService gds;
    
    public class Request {
        
        private final String path;
        private GraphDatabaseService gds;
        private WebResource.Builder resource;
        private int status;
        private URI location;
        private Object entity;
        
        public Request(String path, String baseUri, GraphDatabaseService gds) 
        {
            this.path = path;
            this.gds = gds;
            resource = client
                    .resource(mergeUri(baseUri, path))
                    .accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON);
        }
        


        public Request get() {
            return handle("GET", resource.get(ClientResponse.class));
        }

        public Request post(Object data) {
            return handle("POST", resource.post(ClientResponse.class, formatJson(data)));
        }

        public Request post() {
            return handle("POST", resource.post(ClientResponse.class));
        }

        public Request put(Object data) {
            return handle("PUT", resource.put(ClientResponse.class, formatJson(data)));
        }

        public Request delete() {
            return handle("DELETE", resource.delete(ClientResponse.class));
        }

        private Request handle(String method, ClientResponse response) {
            status = response.getStatus();
            location = response.getLocation();
            readEntityIfPossible(response);
            response.close();
            //System.out.printf(method + " from [%s], status code [%d] location: %s, returned data: %n%s\n", path, status, location, entity);
            return this;
        }

        private void readEntityIfPossible(ClientResponse response) {
            if (status != Response.Status.NO_CONTENT.getStatusCode() && response.getLength() > 0) {
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
                throw new RuntimeException("Error reading response object "+this, e);
            }
        }

        @Override
        public String toString() {
            return "Request{" +
                    "status=" + status +
                    ", location=" + location +
                    ", entity=" + entity +
                    ", path='" + path + '\'' +
                    '}';
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

        public Request created() {
            return assertStatus(Response.Status.CREATED);
        }

        public Request ok() {
            return assertStatus(Response.Status.OK);
        }

        public Request location(String expected) {
            final String value = this.location.toString();
            assertTrue("Location " + expected + " matches " + value, value.matches(expected));
            return this;
        }

        public String location()
        {
            return this.location.toString();
        }

        public Request expect(Object value) {
            assertEquals("Result ", value, this.entity);
            return this;
        }

        public Request expect(String path, Object value) {
            assertEquals("Result[" + path + "]", value, getPath(path));
            return this;
        }

        public Request expectUri(String path, Object value) {
            assertEquals("Result[" + path + "]", mergeUri(baseUri, value.toString()), getPath(path));
            return this;
        }

        private Object getPath(String path) {
            try {
                final String[] parts = path.split("[./]");
                Map map = (Map) entity;
                for (int i = 0; i < parts.length - 1; i++) {
                    String part = parts[i];
                    map = (Map) map.get(part);
                }
                return map.get(parts[parts.length - 1]);
            } catch(NullPointerException e) {
                throw new RuntimeException("Expected response to the following path of keys: " + path);
            }
        }

        public Request assertStatus(Response.Status expectedStatus) {
            assertEquals("status code is not " + expectedStatus, expectedStatus.getStatusCode(), status);
            return this;
        }

        public void compareNodeProperties(String... props) {
            checkPropertyContainer(node(), props);
        }

        private void checkPropertyContainer(PropertyContainer container, String[] props) {
            Map data = data();
            for (String prop : props) {
                checkProperty(container, prop, data.get(prop));
            }
        }

        private void checkProperty(PropertyContainer container, String prop, Object value) {
            if (prop.startsWith("!")) {
                assertFalse(container+" has not "+prop,container.hasProperty(prop.substring(1)));
                return;
            }
            assertEquals(container+" "+prop+": ",container.getProperty(prop), value);
        }

        public void checkRelationship(String...props) {
            checkPropertyContainer(relationship(), props);
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

        public Request notFound() {
            assertStatus(Response.Status.NOT_FOUND);
            return this;
        }

        public Request noContent() {
            assertStatus(Response.Status.NO_CONTENT);
            return this;
        }

        public Request checkNodeProperty(long id, String prop, Object value) {
            checkProperty(gds.getNodeById(id),prop,value);
            return this;
        }
        public Request checkRelationship(long id, String prop, Object value) {
            checkProperty(gds.getRelationshipById(id),prop,value);
            return this;
        }
        
    }

    public REST(String baseUri, GraphDatabaseService gds) {
        this.baseUri = baseUri;
        this.gds = gds;
    }

    private static String mergeUri(String base, String path) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        return (base + "/" + path).replaceAll("([^:]/)/", "$1");
    }

    public Request from(String path) {
        return new Request(path, baseUri, gds);
    }

    public Request to(String path) {
        return new Request(path, baseUri, gds);
    }


}
