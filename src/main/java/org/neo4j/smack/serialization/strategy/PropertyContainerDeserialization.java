package org.neo4j.smack.serialization.strategy;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is a place holder API, its implementation is meant to be modified
 * further down the road. Basically, it is meant to be
 * set up to allow streaming deserialization of key/value
 * properties, without creating garbage and without holding
 * the full set of properties in memory.
 * 
 * As a consumer of these, you can iterate over the properties
 * inside. This allows for future extending this to deserialize
 * on the fly while properties are being created.
 *
 * We don't use the iterator interface directly, because that 
 * would complicate things garbage-wise, since then we have to create
 * wrapping Map.Entry instances for each property as well.
 */
public class PropertyContainerDeserialization {

    private Iterator<Entry<String, Object>> properties;
    private Entry<String, Object> currentProperty;

    // TODO: Replace this, have it take a ChannelBuffer or 
    // something that does not force us to create new maps
    // or hold the full result in memory
    protected void setProperties(Iterator<Map.Entry<String, Object>> properties) 
    {
        this.properties = properties;
    }
    
    public void nextProperty() 
    {
        this.currentProperty = properties.next();
    }
    
    public boolean hasMoreProperties() 
    {
        return properties.hasNext();
    }
    
    // TODO: In the future, we could expand this further,
    // by returning some re-useable (eg. garbage-free) data
    // structure that allows streaming large values directly
    // from kernel-space backed Channel Buffers into the database.
    public Object propertyValue()
    {
        return currentProperty.getValue();
    }
    
    public String propertyKey()
    {
        return currentProperty.getKey();
    }
    
}
