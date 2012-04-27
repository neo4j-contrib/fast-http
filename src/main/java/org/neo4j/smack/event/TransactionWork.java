package org.neo4j.smack.event;

public interface TransactionWork {

    boolean isTransactional();

    long getTransactionId();

    void setTransactionId(Long txId);

    WorkTransactionMode getTransactionMode();
    
    void setTransactionMode(WorkTransactionMode txMode);
    
}
