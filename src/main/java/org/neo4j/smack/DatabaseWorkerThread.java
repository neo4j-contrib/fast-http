package org.neo4j.smack;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.smack.event.DatabaseWork;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.handler.DatabaseWorkPerformer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.WorkProcessor;

public class DatabaseWorkerThread 
{

    private static final AtomicInteger workerId = new AtomicInteger();
    
    // Each worker thread keeps track of its own transactions
    private TransactionRegistry txs;
    private GraphDatabaseService database;

    private static final int BUFFER_SIZE = 512;

    protected RingBuffer<DatabaseWork> workBuffer;
    private WorkProcessor<DatabaseWork> processor;
    private Thread thread;

    public DatabaseWorkerThread(GraphDatabaseService database, TransactionRegistry txs,
            ExceptionHandler exceptionHandler)
    {
        this.txs = txs;
        this.database = database;

        this.workBuffer = new RingBuffer<DatabaseWork>(DatabaseWork.FACTORY,
                new MultiThreadedClaimStrategy(BUFFER_SIZE),
//                new BusySpinWaitStrategy()
//                new YieldingWaitStrategy() //65189.048239895696 requests/second
//                new SleepingWaitStrategy() //104416.83199331732 requests/second
                new BlockingWaitStrategy()
        );

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
            thread.setName("DabaseWorker-" + workerId.incrementAndGet());
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop()
    {
        processor.halt();
    }

    public void addWork(RequestEvent event)
    {

        long sequenceId = workBuffer.next();
        DatabaseWork work = workBuffer.get(sequenceId);

        if(!event.hasFailed()) {
            work.reset(
                    event.getEndpoint(), 
                    event.getChannel(),
                    event.getIsPersistentConnection(), 
                    event.getPath(), 
                    event.getTransactionId(),
                    event.getTransactionMode(),
                    event.getPathVariables(), 
                    event.getDeserializedContent(),
                    database, 
                    txs);
        } else {
            work.reset(
                    event.getChannel(), 
                    event.getIsPersistentConnection(),  
                    event.getTransactionId(),
                    event.getTransactionMode(), 
                    database, 
                    txs, 
                    event.getFailureCause());
        }

        workBuffer.publish(sequenceId);
    }

}
