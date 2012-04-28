package org.neo4j.smack.integration.api;

import static org.neo4j.helpers.collection.MapUtil.map;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.neo4j.smack.test.util.AbstractRestFunctionalTestBase;

public class RelationshipServiceIT extends AbstractRestFunctionalTestBase {
    
    @Test
    public void testCreateRelationshipWithProperties() throws Exception 
    {
        String nodeOne = rest.to("/db/data/node").post().created().location();
        String nodeTwo = rest.to("/db/data/node").post().created().location();
        rest.to(nodeOne + "/relationships").post(map("to",nodeTwo,"type","LOVES","data",map("since","2012"))).created()
            .location("/db/data/relationship/\\d+")
            .expect("data.since", "2012")
            .compareRelationshipProperties("since");
    }
    
    @Test
    public void testCreateRelationship() throws Exception {
        String nodeOne = rest.to("/db/data/node").post().created().location();
        String nodeTwo = rest.to("/db/data/node").post().created().location();
        rest.to(nodeOne + "/relationships").post(map("to",nodeTwo,"type","LOVES"))
            .created().location("/db/data/relationship/\\d+");
    }

    @Test
    public void testCreateRelationshipWithInvalidProperty() throws Exception {
        String nodeOne = rest.to("/db/data/node").post().created().location();
        String nodeTwo = rest.to("/db/data/node").post().created().location();
        rest.to(nodeOne + "/relationships").post(map("to",nodeTwo,"type","LOVES","data",map("name",null))).assertStatus(Response.Status.BAD_REQUEST);
    }
    
    @Test
    public void testSetRelationshipProperty() throws Exception {
        String nodeOne = rest.to("/db/data/node").post().created().location();
        String nodeTwo = rest.to("/db/data/node").post().created().location();
        String relationship = rest.to(nodeOne + "/relationships").post(map("to",nodeTwo,"type","LOVES"))
            .created().location();
        
        rest.to(relationship + "/properties/foo").put("bar").noContent().checkRelationshipProperty(relationship, "foo", "bar");
    }

    @Test
    public void testReplaceRelationshipProperties() throws Exception {
        String nodeOne = rest.to("/db/data/node").post().created().location();
        String nodeTwo = rest.to("/db/data/node").post().created().location();
        String relationship = rest.to(nodeOne + "/relationships").post(map("to",nodeTwo,"type","LOVES"))
            .created().location();
        
        rest.to(relationship + "/properties/name").put("bar").noContent().checkRelationshipProperty(relationship, "name", "bar");
        rest.to(relationship + "/properties").put(map("foo","bar")).noContent().checkRelationshipProperty(relationship, "foo", "bar").checkRelationshipProperty(relationship, "!name", null);
    }

    @Test
    public void testGetRelationship() throws Exception {
        String nodeOne = rest.to("/db/data/node").post().created().location();
        String nodeTwo = rest.to("/db/data/node").post().created().location();
        String relationship = rest.to(nodeOne + "/relationships").post(map("to",nodeTwo,"type","LOVES"))
            .created().location();
        
        rest.from(relationship).get().ok().expect("self", relationship);
    }
    
    @Test
    public void testDeleteRelationship() throws Exception {
        String nodeOne = rest.to("/db/data/node").post().created().location();
        String nodeTwo = rest.to("/db/data/node").post().created().location();
        String relationship = rest.to(nodeOne + "/relationships").post(map("to",nodeTwo,"type","LOVES"))
            .created().location();
        
        rest.from(relationship).get().ok().expect("self", relationship);
        rest.to(relationship).delete().ok();
        rest.from(relationship).get().notFound();
    }

    @Test
    public void testGetNonExistingRelationship() throws Exception {
        rest.from("/db/data/relationship/" + 9999).get().notFound();
    }
}
