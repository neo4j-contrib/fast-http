package org.neo4j.smack;

import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.smack.event.DatabaseInvocationEvent;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.event.ResponseEvent;
import org.neo4j.smack.handler.DatabaseWorkHandler;

import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkProcessor;

public class DatabaseWorkerThread {

    // Each worker thread keeps track of its own transactions
    private TransactionRegistry txs;
    private Database database;
    
    private static final int BUFFER_SIZE = 512;
    
    protected RingBuffer<DatabaseInvocationEvent> workBuffer;
    private WorkProcessor<DatabaseInvocationEvent> processor;
    private Thread thread;
    
    public DatabaseWorkerThread(Database database, TransactionRegistry txs,
            RingBuffer<ResponseEvent> output, ExceptionHandler exceptionHandler) {
        this.txs = txs;
        this.database = database;
        
        this.workBuffer = new RingBuffer<DatabaseInvocationEvent>(DatabaseInvocationEvent.FACTORY,
                BUFFER_SIZE, ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.YIELDING);
        
        SequenceBarrier serializationBarrier = workBuffer.newBarrier();
        processor = new WorkProcessor<DatabaseInvocationEvent>(workBuffer, serializationBarrier, new DatabaseWorkHandler(database, txs, output), exceptionHandler, new AtomicLong(Sequencer.INITIAL_CURSOR_VALUE));
        workBuffer.setGatingSequences(processor.getSequence());
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
        DatabaseInvocationEvent work = workBuffer.get(sequenceId);

        work.id = event.getId();
        work.isTransactional = transactional;
        work.endpoint = event.getEndpoint();
        work.usesTxAPI = usesTxAPI;
        work.isPersistentConnection = event.getIsPersistentConnection();

        work.invocation.setPath(event.getPath());
        work.invocation.setTxId(txId);
        work.invocation.setPathVariables(event.getPathVariables());
        work.invocation.setDeserializedContent(event.getDeserializedContent());
        work.invocation.setDatabase(database);
        work.invocation.setTxRegistry(txs);
        
        work.setContext(event.getContext());   // TODO contexthandler
        
        workBuffer.publish(sequenceId);
    }

}
