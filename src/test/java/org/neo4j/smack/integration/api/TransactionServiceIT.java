package org.neo4j.smack.integration.api;

import org.junit.Test;
import org.neo4j.smack.test.util.AbstractRestFunctionalTestBase;

public class TransactionServiceIT extends AbstractRestFunctionalTestBase {

    @Test
    public void testCreateAndCommitTx() throws Exception {
        String tx = rest.to("/db/data/tx").post().created().location("/db/data/tx/\\d+").location();
        
        String node = rest.to(tx + "/node").post().location();
        
        rest.to(tx + "/state").put("COMMITTED").ok();
        
        rest.to(node).get().ok();
    }
    
    @Test
    public void testCreateAndRollbackTx() throws Exception {
        String tx = rest.to("/db/data/tx").post().created().location("/db/data/tx/\\d+").location();
        
        String node = rest.to(tx + "/node").post().location();
        
        rest.to(tx + "/state").put("ROLLED_BACK").ok();
        
        rest.to(node).get().notFound();
    }
}
