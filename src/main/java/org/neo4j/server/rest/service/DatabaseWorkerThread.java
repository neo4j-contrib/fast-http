package org.neo4j.server.rest.service;

import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.server.database.Database;
import org.neo4j.server.smack.core.RequestEvent;
import org.neo4j.server.smack.core.ResponseEvent;
import org.neo4j.server.transaction.TransactionRegistry;

import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkProcessor;

public class DatabaseWorkerThread {

    // Each worker thread keeps track of its own transactions
    private TransactionRegistry txs;
    private Database database;
    
    private static final int BUFFER_SIZE = 512;
    
    protected RingBuffer<DatabaseWork> workBuffer;
    private WorkProcessor<DatabaseWork> processor;
    private Thread thread;
    
    public DatabaseWorkerThread(Database database, TransactionRegistry txs,
            RingBuffer<ResponseEvent> output, ExceptionHandler exceptionHandler) {
        this.txs = txs;
        this.database = database;
        
        this.workBuffer = new RingBuffer<DatabaseWork>(DatabaseWork.FACTORY,
                BUFFER_SIZE, ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.YIELDING);
        
        SequenceBarrier serializationBarrier = workBuffer.newBarrier();
        workBuffer.setGatingSequences(new Sequence()); // TODO
        processor = new WorkProcessor<DatabaseWork>(workBuffer, serializationBarrier, new DatabaseWorkHandler(database, txs, output), exceptionHandler, new AtomicLong(Sequencer.INITIAL_CURSOR_VALUE));
    }
    
    public void start() {
        if(thread == null) {
            thread = new Thread(processor);
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    public void stop() {
        processor.halt();
    }
    
    public void addWork(RequestEvent event, Long txId, boolean transactional, boolean usesTxAPI) {
        
        long sequenceId = workBuffer.next();
        DatabaseWork work = workBuffer.get(sequenceId);
        
        work.isTransactional = transactional;
        work.endpoint = event.getEndpoint();
        work.usesTxAPI = usesTxAPI;

        work.request.setTxId(txId);
        work.request.setPathVariables(event.getPathVariables());
        work.request.setDeserializedContent(event.getDeserializedContent());
        work.request.setDatabase(database);
        work.request.setTxRegistry(txs);
        
        work.setFailure(null);
        
        work.setContext(event.getContext());   // TODO contexthandler
        workBuffer.publish(sequenceId);
    }

}
