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
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.event.WorkTransactionMode;

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

    private final Database database;
    private long txIds = 0l;
    private final DatabaseWorkerThread[] workers = new DatabaseWorkerThread[NUM_DATABASE_WORK_EXECUTORS];
    private final ExceptionHandler exceptionHandler;

    public DatabaseWorkDivider(Database database, ExceptionHandler exceptionHandler) {
        this.database = database;
        this.exceptionHandler = exceptionHandler;
        start();
    }

    @Override
    public void onEvent(RequestEvent event) {
        // TODO: Move the txMode and txId assignment to it's own handler?
        WorkTransactionMode txMode = WorkTransactionMode.NO_TRANSACTION;
        
        if(event.getEndpoint().isTransactional()) {
            txMode = WorkTransactionMode.OPEN_TRANSACTION;
        }

        // Did client supply a transaction id?
        Long txId = event.getPathVariables().getParamAsLong("tx_id");
        if (txId == null) 
        {
            // Nope, generate one
            txId = txIds++;
            
            // If this seemed like an open transaction, since client
            // did not provide a tx id, it is a single transaction.
            if(txMode == WorkTransactionMode.OPEN_TRANSACTION) {
                txMode = WorkTransactionMode.SINGLE_TRANSACTION;
            }
        }

        // Pick worker
        int workerId = (int) event.getConnectionId() % NUM_DATABASE_WORK_EXECUTORS;
        workers[workerId].addWork(event, txId, txMode);
    }

    public void stop() {
        for (DatabaseWorkerThread worker : workers) {
            stopWorker(worker);
        }
    }

    private void start() {
        for (int i = 0; i < NUM_DATABASE_WORK_EXECUTORS; i++) {
            DatabaseWorkerThread worker = new DatabaseWorkerThread(database, new TransactionRegistry(database.getGraphDB()), exceptionHandler);
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
