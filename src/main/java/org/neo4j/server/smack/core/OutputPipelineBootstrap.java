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

public class OutputPipelineBootstrap {
    
    private static final int NUM_EVENT_PROCESSORS = 2;
    private static final int BUFFER_SIZE = 1024 * 2;

    private RingBuffer<ResponseEvent> ringBuffer;

    private SerializationHandler serializationHandler;

    private WorkProcessor<ResponseEvent> serializationProcessor;

    private ExecutorService workers;

    public OutputPipelineBootstrap(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
    }

    public void start() {
        ringBuffer = new RingBuffer<ResponseEvent>(
                ResponseEvent.FACTORY,
                BUFFER_SIZE,
                ClaimStrategy.Option.SINGLE_THREADED,
                WaitStrategy.Option.YIELDING);

        final SequenceBarrier serializationBarrier = ringBuffer.newBarrier();
        final AtomicLong workSequence = new AtomicLong(Sequencer.INITIAL_CURSOR_VALUE);

        serializationProcessor = new WorkProcessor<ResponseEvent>(ringBuffer, serializationBarrier, serializationHandler, new WorkExceptionHandler(), workSequence);
        ringBuffer.setGatingSequences(serializationProcessor.getSequence());

        workers = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS, new DaemonThreadFactory());
        workers.submit(serializationProcessor);
    }

    public void stop() {
        serializationProcessor.halt();
    }

    public RingBuffer<ResponseEvent> getRingBuffer() {
        return ringBuffer;
    }

}
