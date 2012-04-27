package org.neo4j.smack.integration.api;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.smack.test.util.AbstractRestFunctionalTestBase;
import org.neo4j.smack.test.util.REST;

public class TransactionServiceIT extends AbstractRestFunctionalTestBase {
    
    @Before
    public void setUp() throws Exception {
        REST.init(getBaseUri(),"",graphdb());
    }

    @Test
    public void testCreateAndCommitTx() throws Exception {
        String tx = REST.to("/db/data/tx").post().created().location("/db/data/tx/\\d+").location();
        
        String node = REST.to(tx + "/node").post().location();
        
        REST.to(tx + "/state").put("COMMITTED").ok();
        
        REST.to(node).get().ok();
    }
    
    @Test
    public void testCreateAndRollbackTx() throws Exception {
        String tx = REST.to("/db/data/tx").post().created().location("/db/data/tx/\\d+").location();
        
        String node = REST.to(tx + "/node").post().location();
        
        REST.to(tx + "/state").put("ROLLED_BACK").ok();
        
        REST.to(node).get().notFound();
    }
}
