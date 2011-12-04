package org.neo4j.smack;

/**
 * @author mh
 * @since 27.11.11
 */
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.smack.handler.WorkExceptionHandler;

import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.Sequencer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkProcessor;


public class PipelineBootstrap<E> {

    private final WorkExceptionHandler exceptionHandler = new WorkExceptionHandler();

    private RingBuffer<E> ringBuffer;

    private List<WorkProcessor<E>> processors = new ArrayList<WorkProcessor<E>>();
    
    private ExecutorService workers;
    
    private final List<WorkHandler<E>> handlers;
    
    private final EventFactory<E> eventFactory;

    public PipelineBootstrap(final EventFactory<E> eventFactory, WorkHandler<E>... handlers) {
        this.handlers = asList(handlers);
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