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
package org.neo4j.server.rest.service;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import org.apache.log4j.Logger;
import org.neo4j.server.database.Database;
import org.neo4j.server.smack.PathVariables;
import org.neo4j.server.smack.core.ExecutionHandler;
import org.neo4j.server.smack.core.RequestEvent;
import org.neo4j.server.smack.core.ResponseEvent;
import org.neo4j.server.smack.core.WorkExceptionHandler;
import org.neo4j.server.transaction.TransactionRegistry;

public class DatabaseWorkerPool implements ExecutionHandler {

    private final static Logger logger = Logger.getLogger(DatabaseWorkerPool.class);

    private static final int NUM_DATABASE_WORK_EXECUTORS = 4;
    
    private RingBuffer<ResponseEvent> output;
    private Database database;
    
    private long txIds = 0l;
    
    private DatabaseWorkerThread [] workers = new DatabaseWorkerThread[NUM_DATABASE_WORK_EXECUTORS];

    private ExceptionHandler exceptionHandler;

    public DatabaseWorkerPool(Database database) {
        this.database = database;
        
        // TODO: Exception handling
        this.exceptionHandler = new WorkExceptionHandler();
    }
    
    @Override
    public void start() {
        // Set up workers
        for(int i=0; i<NUM_DATABASE_WORK_EXECUTORS;i++) {
            DatabaseWorkerThread worker = new DatabaseWorkerThread(database, new TransactionRegistry(database.getGraphDB()), output, exceptionHandler);
            workers[i] = worker;
            worker.start();
        }
    }
    
    @Override
    public void stop() {
        for(DatabaseWorkerThread worker : workers) {
            stopWorker(worker);
        }
    }

    private void stopWorker(DatabaseWorkerThread worker) {
        try {
            if (worker==null) {
                logger.warn("Worker is null");
                return;
            }
            worker.stop();
        } catch(Exception e) {
            logger.error("Error stopping worker", e);
        }
    }

    @Override
    public void setOutputBuffer(RingBuffer<ResponseEvent> output) {
        this.output = output;
    }

    @Override
    public void onEvent(RequestEvent event) {
        try {
            
            
            boolean usesTxAPI = true;
            final PathVariables pathVariables = event.getPathVariables();
            Long txId = pathVariables.getParamAsLong("tx_id");
            if(txId == null) {
                txId = txIds++;
                usesTxAPI = false;
            }
            
            Object transactionalParam = event.getEndpoint().getParameter("transactional");
            boolean transactional = transactionalParam!=null && transactionalParam.equals("true");

            // Pick worker
            int workerId = (int) (txId % NUM_DATABASE_WORK_EXECUTORS);
            DatabaseWorkerThread worker = workers[workerId];

            worker.addWork(event, txId, transactional, usesTxAPI);
            
        } catch(Exception e) {
            logger.error("onEvent "+event,e);
        }
    }

}
