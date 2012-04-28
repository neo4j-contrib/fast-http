package org.neo4j.smack.serialization.strategy;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.neo4j.graphdb.DynamicRelationshipType;

public class RelationshipCreationDescription extends PropertyContainerDeserialization {

    public static class Factory extends BasePoolableObjectFactory<RelationshipCreationDescription> {

        @Override
        public RelationshipCreationDescription makeObject() throws Exception
        {
            return new RelationshipCreationDescription();
        }

    }

    private DynamicRelationshipType type;
    private long endNodeId;

    public DynamicRelationshipType getType()
    {
        return type;
    }

    public void setType(DynamicRelationshipType type) {
        this.type = type;
    }

    public void setEndNodeId(long nodeId)
    {
        this.endNodeId = nodeId;
    }

    public long getEndNodeId()
    {
        return endNodeId;
    }
    
}
