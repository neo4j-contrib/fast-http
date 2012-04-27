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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.smack.DatabaseWorkerThread;
import org.neo4j.smack.ThreadTransactionManagement;
import org.neo4j.smack.TransactionRegistry;
import org.neo4j.smack.event.RequestEvent;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * Takes a prepared RequestEvent, packages it into DatabaseWork, and
 * assigns that work to a worker based on the connection ID (same connection
 * always goes to the same worker).
 */
public class DatabaseWorkDivider implements WorkHandler<RequestEvent> {

    private final static Logger logger = Logger.getLogger(DatabaseWorkDivider.class);

    private static final int NUM_DATABASE_WORK_EXECUTORS = 4;

    private final GraphDatabaseService database;
    private final DatabaseWorkerThread[] workers = new DatabaseWorkerThread[NUM_DATABASE_WORK_EXECUTORS];
    private final ExceptionHandler exceptionHandler;

    public DatabaseWorkDivider(GraphDatabaseService database, ExceptionHandler exceptionHandler) {
        this.database = database;
        this.exceptionHandler = exceptionHandler;
        start();
    }

    @Override
    public void onEvent(RequestEvent event) 
    {
        int workerId = (int) (event.getConnectionId() % NUM_DATABASE_WORK_EXECUTORS);
        workers[workerId].addWork(event);
    }

    public void stop() {
        for (DatabaseWorkerThread worker : workers) {
            stopWorker(worker);
        }
    }

    private void start() {
        for (int i = 0; i < NUM_DATABASE_WORK_EXECUTORS; i++) {
            TransactionRegistry txs = new TransactionRegistry(database);
            ThreadTransactionManagement txManage = new ThreadTransactionManagement(txs);
            DatabaseWorkerThread worker = new DatabaseWorkerThread(database, txs, txManage, exceptionHandler);
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

}
