package org.neo4j.smack.pipeline.core.event;

import org.neo4j.smack.pipeline.event.WorkTransactionMode;


public interface TransactionWork {

    boolean isTransactional();

    long getTransactionId();

    void setTransactionId(Long txId);

    WorkTransactionMode getTransactionMode();
    
    void setTransactionMode(WorkTransactionMode txMode);
    
}
