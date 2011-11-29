package org.neo4j.server.rest.service;

import org.neo4j.server.smack.Endpoint;
import org.neo4j.server.smack.InvocationRequest;
import org.neo4j.server.smack.InvocationResult;

import com.lmax.disruptor.EventFactory;

public class DatabaseWork {
    
    public static EventFactory<DatabaseWork> FACTORY = new EventFactory<DatabaseWork>() {
        public DatabaseWork newInstance() {
            return new DatabaseWork();
        }
    };
    
    public boolean isTransactional = false;
    
    public InvocationRequest request = new InvocationRequest();
    public InvocationResult result = new InvocationResult();

    public Endpoint endpoint;

    /**
     * True if this unit of work is expected to be performed within
     * an already existing and ongoing transaction.
     */
    public boolean usesTxAPI;
}
