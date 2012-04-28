package org.neo4j.smack.pipeline;

/**
 * @author mh
 * @since 27.11.11
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkProcessor;


public class RingBufferWorkPipeline<E> {

    protected RingBuffer<E> ringBuffer;

    private final ExceptionHandler exceptionHandler;

    private List<WorkProcessor<E>> processors = new ArrayList<WorkProcessor<E>>();
    
    private ExecutorService workers;
    
    private final List<WorkHandler<E>> handlers = new ArrayList<WorkHandler<E>>();
    
    private final EventFactory<E> eventFactory;

    private String nameForThreads;

    public RingBufferWorkPipeline(String nameForThreads, final EventFactory<E> eventFactory, final ExceptionHandler exceptionHandler) {
        this.nameForThreads = nameForThreads;
        this.eventFactory = eventFactory;
        this.exceptionHandler = exceptionHandler;
    }

    public void start() {
        if (handlers.isEmpty()) throw new IllegalStateException("No Handlers configured on Pipeline");
        final int numEventProcessors = handlers.size();
        workers = Executors.newFixedThreadPool(numEventProcessors, new DaemonThreadFactory(nameForThreads));

        final int bufferSize = 1024 * 4;
        ringBuffer = new RingBuffer<E>(
                eventFactory,
                new MultiThreadedClaimStrategy(bufferSize),
                new BusySpinWaitStrategy());

        WorkProcessor<E> processor = null;
        for (WorkHandler<E> handler : handlers) {
            processor = scheduleEventProcessor(processor, handler);
            processors.add(processor);
        }
        ringBuffer.setGatingSequences(processor.getSequence());

    }

    private WorkProcessor<E> scheduleEventProcessor(WorkProcessor<E> predecessor, WorkHandler<E> handler) {
        WorkProcessor<E> newProcessor = new WorkProcessor<E>(ringBuffer, barrierFor(predecessor), handler, exceptionHandler, newSequence());
        workers.submit(newProcessor);
        return newProcessor;
    }

    private SequenceBarrier barrierFor(WorkProcessor<E> predecessor) {
        if (predecessor == null) return ringBuffer.newBarrier();
        return ringBuffer.newBarrier(predecessor.getSequence());
    }

    private AtomicLong newSequence() {
        return new AtomicLong(Sequencer.INITIAL_CURSOR_VALUE);
    }

    public void stop() {
        for (WorkProcessor<E> processor : processors) {
            processor.halt();
        }
        workers.shutdown();
    }

    public RingBuffer<E> getRingBuffer() {
        return ringBuffer;
    }

    public void addHandler(WorkHandler<E> handler)
    {
        handlers.add(handler);
    }

}