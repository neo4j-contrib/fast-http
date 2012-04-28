package org.neo4j.smack.api;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.routing.UrlReverseLookerUpper;
import org.neo4j.smack.serialization.strategy.PropertyContainerDeserialization;

public class BasePropertyContainerService 
{
    
    protected UrlReverseLookerUpper url = new UrlReverseLookerUpper();

    protected long getNodeId(Invocation invocation)
    {
        return invocation.getLongParameter(UrlReverseLookerUpper.NODE_ID_NAME, -1l);
    }
    
    protected long getRelationshipId(Invocation invocation) 
    {
        return invocation.getLongParameter(UrlReverseLookerUpper.RELATIONSHIP_ID_NAME, -1l);
    }
    
    protected String getPropertyKey(Invocation invocation) 
    {
        return invocation.getStringParameter(UrlReverseLookerUpper.PROPERTY_KEY_NAME, null);
    }

    protected void setProperties(PropertyContainer entity, PropertyContainerDeserialization props)
    {
        while(props.hasMoreProperties()) 
        {
            props.nextProperty();
            entity.setProperty(props.propertyKey(), props.propertyValue());
        }
    }

    protected void removeAllProperties(PropertyContainer entity)
    {
        for(String key: entity.getPropertyKeys()) 
        {
            entity.removeProperty(key);
        }
    }
    
}
