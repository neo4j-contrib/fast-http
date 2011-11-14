package org.neo4j.server.rest.service;

import com.lmax.disruptor.*;

import java.util.concurrent.atomic.AtomicLong;

public class BufferedWorkExecutor<T> {

    private static final int BUFFER_SIZE = 512;
    
    protected RingBuffer<T> workBuffer;
    private WorkProcessor<T> processor;
    private Thread thread;
    
    public BufferedWorkExecutor(WorkHandler<T> workHandler, EventFactory<T> factory, ExceptionHandler exceptionHandler) {
        workBuffer = new RingBuffer<T>(factory,
                BUFFER_SIZE, ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.YIELDING);
        final SequenceBarrier serializationBarrier = workBuffer.newBarrier();
        workBuffer.setGatingSequences(new Sequence()); // TODO
        processor = new WorkProcessor<T>(workBuffer, serializationBarrier, workHandler, exceptionHandler, new AtomicLong(Sequencer.INITIAL_CURSOR_VALUE));
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
    
}
