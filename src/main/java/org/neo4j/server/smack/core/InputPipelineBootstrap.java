/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.smack.core;

import com.lmax.disruptor.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


public class InputPipelineBootstrap {

    private static final int NUM_EVENT_PROCESSORS = 3;
    private static final int BUFFER_SIZE = 1024 * 4;

    private final WorkExceptionHandler exceptionHandler = new WorkExceptionHandler();

    private RingBuffer<RequestEvent> ringBuffer;

    private RoutingHandler routingHandler;
    private DeserializationHandler deserializationHandler;
    private WorkHandler<RequestEvent> executionHandler;

    private WorkProcessor<RequestEvent> routingProcessor;
    private WorkProcessor<RequestEvent> deserializationProcessor;
    private WorkProcessor<RequestEvent> executionProcessor;

    private ExecutorService workers;

    public InputPipelineBootstrap(RoutingHandler routingHandler,
            DeserializationHandler deserializationHandler,
            WorkHandler<RequestEvent> executionHandler) {
        this.routingHandler = routingHandler;
        this.deserializationHandler = deserializationHandler;
        this.executionHandler = executionHandler;
    }

    public void start() {
        workers = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS, new DaemonThreadFactory());

        ringBuffer = new RingBuffer<RequestEvent>(
                RequestEvent.FACTORY,
                BUFFER_SIZE,
                ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.YIELDING);

        routingProcessor = scheduleEventProcessor(null, routingHandler);
        deserializationProcessor = scheduleEventProcessor(routingProcessor, deserializationHandler);
        executionProcessor = scheduleEventProcessor(deserializationProcessor,executionHandler);

        ringBuffer.setGatingSequences(executionProcessor.getSequence());

    }

    private WorkProcessor<RequestEvent> scheduleEventProcessor(WorkProcessor<RequestEvent> predecessor, WorkHandler<RequestEvent> handler) {
        routingProcessor = new WorkProcessor<RequestEvent>(ringBuffer, barrierFor(predecessor), handler, exceptionHandler, newSequence());
        workers.submit(routingProcessor);
        return routingProcessor;
    }

    private SequenceBarrier barrierFor(WorkProcessor<RequestEvent> predecessor) {
        if (predecessor == null) return ringBuffer.newBarrier();
        return ringBuffer.newBarrier(predecessor.getSequence());
    }

    private AtomicLong newSequence() {
        return new AtomicLong(Sequencer.INITIAL_CURSOR_VALUE);
    }

    public void stop() {
        routingProcessor.halt();
        deserializationProcessor.halt();
        executionProcessor.halt();
        workers.shutdown();
    }

    public RingBuffer<RequestEvent> getRingBuffer() {
        return ringBuffer;
    }
    
}