package org.neo4j.server.smack.core;

/**
 * @author mh
 * @since 27.11.11
 */
import com.lmax.disruptor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Arrays.asList;


public class PipelineBootstrap<E> {

    private final WorkExceptionHandler exceptionHandler = new WorkExceptionHandler();

    private RingBuffer<E> ringBuffer;

    private final List<WorkHandler<E>> handlers;
    private List<WorkProcessor<E>> processors=new ArrayList<WorkProcessor<E>>();
    private ExecutorService workers;
    private final EventFactory<E> eventFactory;

    public PipelineBootstrap(final EventFactory<E> eventFactory, WorkHandler<E>... handlers) {
        this.handlers=asList(handlers);
        this.eventFactory = eventFactory;
    }

    public void start() {
        if (handlers.isEmpty()) throw new IllegalStateException("No Handlers configured on Pipeline");
        final int numEventProcessors = handlers.size();
        workers = Executors.newFixedThreadPool(numEventProcessors, new DaemonThreadFactory());

        final int bufferSize = 1024 * 4;
        ringBuffer = new RingBuffer<E>(
                eventFactory,
                bufferSize,
                ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.YIELDING);

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

}