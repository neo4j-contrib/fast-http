package org.neo4j.smack;

import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.smack.event.DatabaseWork;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.event.WorkTransactionMode;
import org.neo4j.smack.handler.DatabaseWorkPerformer;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
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
            ExceptionHandler exceptionHandler)
    {
        this.txs = txs;
        this.database = database;

        this.workBuffer = new RingBuffer<DatabaseWork>(DatabaseWork.FACTORY,
                BUFFER_SIZE);

        SequenceBarrier serializationBarrier = workBuffer.newBarrier();
        processor = new WorkProcessor<DatabaseWork>(workBuffer,
                serializationBarrier, new DatabaseWorkPerformer(database, txs),
                exceptionHandler,
                new AtomicLong(Sequencer.INITIAL_CURSOR_VALUE));
        workBuffer.setGatingSequences(processor.getSequence());
    }

    public void start()
    {
        if (thread == null)
        {
            thread = new Thread(processor);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop()
    {
        processor.halt();
    }

    public void addWork(RequestEvent event, Long txId,
            WorkTransactionMode txMode)
    {

        long sequenceId = workBuffer.next();
        DatabaseWork work = workBuffer.get(sequenceId);

        work.reset(event.getEndpoint(), event.getChannel(),
                event.getIsPersistentConnection(), event.getPath(), txId, txMode,
                event.getPathVariables(), event.getDeserializedContent(),
                database, txs);

        workBuffer.publish(sequenceId);
    }

}
