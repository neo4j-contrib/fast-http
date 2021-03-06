package org.neo4j.smack.serialization.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.Test;


public class TestTransactionStateDeserializationStrategy extends SerializationStrategyTestBase {

    @Test
    public void shouldDeserializeCommittedValue() throws UnsupportedEncodingException 
    {   
        assertThat(deserialize("\"COMMITTED\"".getBytes("UTF-8"), new TransactionStateDeserializationStrategy()),
                is(TransactionStateDeserialization.COMMITTED));
    }

    @Test
    public void shouldDeserializeRollbackValue() throws UnsupportedEncodingException 
    {
        assertThat(deserialize("\"ROLLED_BACK\"".getBytes("UTF-8"), new TransactionStateDeserializationStrategy()),
                is(TransactionStateDeserialization.ROLLED_BACK));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnInvalidState() throws UnsupportedEncodingException 
    {    
        deserialize("\"BLAH\"".getBytes("UTF-8"), new TransactionStateDeserializationStrategy());
    }
    
}
