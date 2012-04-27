package org.neo4j.smack.serialization.strategy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.neo4j.smack.domain.TransactionState;
import org.neo4j.smack.serialization.JsonDeserializer;


public class TestTxStateDeserializationStrategy {

    @Test
    public void shouldDeserializeCommittedValue() throws UnsupportedEncodingException 
    {    
        InputStream in = new ByteArrayInputStream("\"COMMITTED\"".getBytes("UTF-8"));
        JsonDeserializer deserializer = new JsonDeserializer(new JsonFactory(new ObjectMapper()), in);
        
        TxStateDeserializationStrategy strategy = new TxStateDeserializationStrategy();
        
        assertThat(strategy.deserialize(deserializer),is(TransactionState.COMMITTED));
    }
    
    @Test
    public void shouldDeserializeRollbackValue() throws UnsupportedEncodingException 
    {    
        InputStream in = new ByteArrayInputStream("\"ROLLED_BACK\"".getBytes("UTF-8"));
        JsonDeserializer deserializer = new JsonDeserializer(new JsonFactory(new ObjectMapper()), in);
        
        TxStateDeserializationStrategy strategy = new TxStateDeserializationStrategy();
        
        assertThat(strategy.deserialize(deserializer),is(TransactionState.ROLLED_BACK));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnInvalidState() throws UnsupportedEncodingException 
    {    
        InputStream in = new ByteArrayInputStream("\"BLAH\"".getBytes("UTF-8"));
        JsonDeserializer deserializer = new JsonDeserializer(new JsonFactory(new ObjectMapper()), in);
        
        TxStateDeserializationStrategy strategy = new TxStateDeserializationStrategy();
        
        strategy.deserialize(deserializer);
    }
    
}
