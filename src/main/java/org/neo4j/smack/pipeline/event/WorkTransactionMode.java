package org.neo4j.smack.pipeline.event;

public enum WorkTransactionMode {

    /**
     * Perform an invocation in a single transaction,
     * opened and closed specifically for this piece of
     * work.
     */
    SINGLE_TRANSACTION,
    
    /**
     * Perform an invocation in an ongoing, already opened
     * transaction.
     */
    OPEN_TRANSACTION,
    
    /**
     * Perform this work outside of the scope of a transaction.
     */
    NO_TRANSACTION
    
}
