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
package org.neo4j.smack.handler;

import org.apache.log4j.Logger;
import org.neo4j.smack.Database;
import org.neo4j.smack.DatabaseWorkerThread;
import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.annotation.Transactional;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.event.ResponseEvent;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;

public class ExecutionHandler implements WorkHandler<RequestEvent> {

    private final static Logger logger = Logger.getLogger(ExecutionHandler.class);

    private static final int NUM_DATABASE_WORK_EXECUTORS = 4;

    private final RingBuffer<ResponseEvent> output;
    private final Database database;
    private long txIds = 0l;
    private final DatabaseWorkerThread[] workers = new DatabaseWorkerThread[NUM_DATABASE_WORK_EXECUTORS];
    private final ExceptionHandler exceptionHandler;

    public ExecutionHandler(Database database, RingBuffer<ResponseEvent> output, ExceptionHandler exceptionHandler) {
        this.database = database;
        this.output = output;
        this.exceptionHandler = exceptionHandler;
        start();
    }

    public void stop() {
        for (DatabaseWorkerThread worker : workers) {
            stopWorker(worker);
        }
    }

    private void start() {
        for (int i = 0; i < NUM_DATABASE_WORK_EXECUTORS; i++) {
            DatabaseWorkerThread worker = new DatabaseWorkerThread(database, new TransactionRegistry(database.getGraphDB()), output, exceptionHandler);
            workers[i] = worker;
            worker.start();
        }
    }

    private void stopWorker(DatabaseWorkerThread worker) {
        try {
            if (worker == null) {
                logger.warn("Worker is null");
                return;
            }
            worker.stop();
        } catch (Exception e) {
            logger.error("Error stopping worker", e);
        }
    }

    @Override
    public void onEvent(RequestEvent event) {
        if (event.hasFailed()) return;
        boolean transactional = event.getEndpoint().hasAnnotation(Transactional.class);

        boolean usesTxAPI = true;

        // Did client supply a transaction id?
        Long txId = event.getPathVariables().getParamAsLong("tx_id");
        if (txId == null) {
            // Nope, generate one
            txId = txIds++;
            usesTxAPI = false;
        }

        // Pick worker
        int workerId = (int) (((Long)event.getContext().getAttachment()) % NUM_DATABASE_WORK_EXECUTORS);
        workers[workerId].addWork(event, txId, transactional, usesTxAPI);
    }

}
