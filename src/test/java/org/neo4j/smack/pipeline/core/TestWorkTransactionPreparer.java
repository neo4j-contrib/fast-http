package org.neo4j.smack.pipeline.core;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.neo4j.smack.pipeline.core.WorkTransactionPreparer;
import org.neo4j.smack.pipeline.core.event.TransactionWork;
import org.neo4j.smack.pipeline.event.WorkTransactionMode;


public class TestWorkTransactionPreparer 
{
    @Test
    public void shouldSetNoTransactionIfNoTxIdAndNotTransactional()
    {
        WorkTransactionPreparer prepper = new WorkTransactionPreparer();
        TransactionWork txWork = mock(TransactionWork.class);
        
        when(txWork.getTransactionId()).thenReturn(-1l);
        when(txWork.isTransactional()).thenReturn(false);
        
        prepper.prepare(txWork);
        
        verify(txWork).setTransactionMode(WorkTransactionMode.NO_TRANSACTION);
        
        // TODO: Is it necessary that it generates a tx id for non-transactional work?
        verify(txWork).setTransactionId(0l);
    }
    
    @Test
    public void shouldSetOpenTransactionToTransactionalWorkWithTxId()
    {
        WorkTransactionPreparer prepper = new WorkTransactionPreparer();
        TransactionWork txWork = mock(TransactionWork.class);
        
        when(txWork.getTransactionId()).thenReturn(1l);
        when(txWork.isTransactional()).thenReturn(true);
        
        prepper.prepare(txWork);
        
        verify(txWork).setTransactionMode(WorkTransactionMode.OPEN_TRANSACTION);
        verify(txWork, never()).setTransactionId(anyLong());
    }
    
    @Test
    public void shouldSetSingleTransactionToTransactionalWorkWithNoTxId()
    {
        WorkTransactionPreparer prepper = new WorkTransactionPreparer();
        TransactionWork txWork = mock(TransactionWork.class);
        
        when(txWork.getTransactionId()).thenReturn(-1l);
        when(txWork.isTransactional()).thenReturn(true);
        
        prepper.prepare(txWork);
        
        verify(txWork).setTransactionMode(WorkTransactionMode.SINGLE_TRANSACTION);
        verify(txWork).setTransactionId(anyLong());
    }
    
}
