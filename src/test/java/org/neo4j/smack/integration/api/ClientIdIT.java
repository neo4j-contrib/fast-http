package org.neo4j.smack.integration.api;

import static org.neo4j.helpers.collection.MapUtil.map;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.smack.test.util.AbstractRestFunctionalTestBase;

public class ClientIdIT extends AbstractRestFunctionalTestBase {
    
    @Test
    @Ignore
    public void testCreateNodeAndRelationshipsWithClientId() throws Exception {
        rest.to("/db/data/tx/c1").put();
        
        String nodeOne = rest.to("/db/data/tx/c1/node/c2").put().location();
        String nodeTwo = rest.to("/db/data/tx/c1/node/c3").put().location();
        String rel = rest.to("/db/data/tx/c1/node/c2/relationship/c4").put(map("to","/db/data/node/c3","type","KNOWS")).location();
        
        rest.to("/db/data/tx/c1/state").put("COMMITTED").ok();
        
        rest.to(nodeOne).get().ok();
        rest.to(nodeTwo).get().ok();
        rest.to(rel).get().ok();
    }
}
