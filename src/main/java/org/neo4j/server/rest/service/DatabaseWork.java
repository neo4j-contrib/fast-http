package org.neo4j.server.rest.service;

import org.neo4j.server.smack.Endpoint;
import org.neo4j.server.smack.InvocationRequest;
import org.neo4j.server.smack.InvocationResponse;

import com.lmax.disruptor.EventFactory;

public class DatabaseWork {
    
    public static EventFactory<DatabaseWork> FACTORY = new EventFactory<DatabaseWork>() {
        public DatabaseWork newInstance() {
            return new DatabaseWork();
        }
    };
    
    public boolean isTransactional = false;
    public long txId = -1l;
    
    public InvocationRequest request = new InvocationRequest();
    public InvocationResponse response = new InvocationResponse();

    public Endpoint endpoint;

    /**
     * True if this unit of work is expected to be performed within
     * an already existing and ongoing transaction.
     */
    public boolean usesTxAPI;

}
