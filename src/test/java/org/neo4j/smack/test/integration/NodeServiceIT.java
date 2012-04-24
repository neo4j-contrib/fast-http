package org.neo4j.smack.test.integration;

import static org.neo4j.helpers.collection.MapUtil.map;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.smack.REST;
import org.neo4j.smack.test.util.AbstractRestFunctionalTestBase;

/**
 * @author mh
 * @since 14.11.11
 */
public class NodeServiceIT extends AbstractRestFunctionalTestBase {
    
    @Before
    public void setUp() throws Exception {
        REST.init(getBaseUri(),"",graphdb());
    }

    @Test
    public void testGetDbInfo() throws Exception {
        REST.from("").get().ok().expectUri("reference_node", "node/0");
    }

    @Test
    public void testCreateNodeWithProperties() throws Exception {
        REST.to("node").post(map("name","John","age",10)).created().location("/node/\\d+").expect("data/name", "John").expect("data.age", 10).compareNodeProperties("age", "name");
    }
    
    @Test
    public void testCreateNode() throws Exception {
        REST.to("node").post().created().location("/node/\\d+").expect("data", map()).compareNodeProperties("!name");
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
        final String rootNodeUri = "node/0";
        REST.from(rootNodeUri).get().ok().expectUri("self", rootNodeUri);
    }

    @Test
    public void testGetNonExistingNode() throws Exception {
        REST.from("/node/" + 9999).get().notFound();
    }
}
