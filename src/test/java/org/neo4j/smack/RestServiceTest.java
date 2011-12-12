package org.neo4j.smack;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.smack.api.DataAPI;
import org.neo4j.test.ImpermanentGraphDatabase;

import javax.ws.rs.core.Response;

import static org.neo4j.helpers.collection.MapUtil.map;

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
        final DataAPI dataApi = new DataAPI(HOST, PORT);
        server.addRoute("", dataApi);
        server.start();
        REST.init(dataApi.getBaseUri(),dataApi.getDataPath(),gds);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
        gds.shutdown();
    }

    @Test
    public void testGetDbInfo() throws Exception {
        REST.from("").get().ok().expectUri("reference_node", "node/0");
    }

    @Test
    public void testCreateNodeWithProperties() throws Exception {
        REST.to("node").post(map("name","John","age",10)).created().location("node/\\d+").expect("data/name", "John").expect("data.age", 10).compareNodeProperties("age", "name");
    }
    @Test
    public void testCreateNode() throws Exception {
        REST.to("node").post().created().location("node/\\d+").expect("data", map()).compareNodeProperties("!name");
    }

    @Test
    public void testCreateNodeWithInvalidProperty() throws Exception {
        REST.to("node").post(map("name",null)).assertStatus(Response.Status.BAD_REQUEST);
    }

    @Test
    public void testSetNodeProperty() throws Exception {
        REST.to("node/0/properties/foo").put("bar").noContent().checkNodeProperty(0, "foo", "bar");
    }

    @Test
    public void testReplaceNodeProperties() throws Exception {
        REST.to("node/0/properties").put(map("foo","bar")).noContent().checkNodeProperty(0, "foo", "bar").checkNodeProperty(0, "!name", null);
    }

    @Test
    public void testGetNode() throws Exception {
        final String rootNodeUri = "node/" + ROOT_NODE_ID;
        REST.from(rootNodeUri).get().ok().expectUri("self", rootNodeUri);
    }

    @Test
    public void testGetNonExistingNode() throws Exception {
        REST.from("node/" + 9999).get().notFound();
    }
}
