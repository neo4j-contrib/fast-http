package org.neo4j.smack.api;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.annotation.Transactional;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Output;
import org.neo4j.smack.routing.UrlReverseLookerUpper;
import org.neo4j.smack.serialization.strategy.NodeSerializationStrategy;
import org.neo4j.smack.serialization.strategy.PropertyContainerDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.PropertyContainerSerializationStrategy;
import org.neo4j.smack.serialization.strategy.PropertyValueDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.PropertyValueSerializationStrategy;

public class NodeService 
{
    private static final UrlReverseLookerUpper url = new UrlReverseLookerUpper();
    
    private static final String NODE_ID_NAME = "node_id";
    private static final String NODE_PROPERTY_KEY_NAME = "key";
    
    private static final String PATH_NODES = "node";
    private static final String PATH_NODE  = PATH_NODES + "/{" + NODE_ID_NAME + "}";
    private static final String PATH_NODE_PROPERTIES = PATH_NODE + "/properties";
    private static final String PATH_NODE_PROPERTY = PATH_NODE_PROPERTIES + "/{" + NODE_PROPERTY_KEY_NAME + "}";

    @POST
    @Transactional
    @Path(PATH_NODES)
    @DeserializeWith(PropertyContainerDeserializationStrategy.class)
    @SerializeWith(NodeSerializationStrategy.class)
    public void createNode(Invocation invocation, Output result) 
    {
        Node node = invocation.getDB().createNode();
        
        setProperties(node, invocation.<Map<String,Object>>getContent());
        
        result.createdAt(url.reverse(node), node);
    }

    @GET
    @Path(PATH_NODE)
    @SerializeWith(NodeSerializationStrategy.class)
    public void getNode(Invocation invocation, Output result)
    {
        System.out.println(getNodeId(invocation));
        result.ok( invocation.getDB().getNodeById(getNodeId(invocation)) );
    }

    @DELETE
    @Transactional
    @Path(PATH_NODE)
    public void deleteNode(Invocation invocation, Output result)
    {
        invocation.getDB().getNodeById(getNodeId(invocation)).delete();
        result.ok();
    }

    @PUT
    @Transactional
    @Path(PATH_NODE_PROPERTIES)
    @DeserializeWith(PropertyContainerDeserializationStrategy.class)
    public void setAllNodeProperties(Invocation invocation, Output result) 
    {
        Node node = invocation.getDB().getNodeById(getNodeId(invocation));
        
        removeAllProperties(node);
        setProperties(node, invocation.<Map<String,Object>>getContent());
        
        result.okNoContent();
    }

    @GET
    @Path(PATH_NODE_PROPERTIES)
    @SerializeWith(PropertyContainerSerializationStrategy.class)
    public void getAllNodeProperties(Invocation invocation, Output result) 
    {
        result.ok(invocation.getDB().getNodeById(getNodeId(invocation)));
    }

    @PUT
    @Transactional
    @Path(PATH_NODE_PROPERTY)
    @DeserializeWith(PropertyValueDeserializationStrategy.class)
    public void setNodeProperty(Invocation invocation, Output result)
    {
        Node node = invocation.getDB().getNodeById(getNodeId(invocation));
        
        node.setProperty(invocation.getStringParameter(NODE_PROPERTY_KEY_NAME), invocation.getContent());
        
        result.okNoContent();
    }

    @GET
    @Path(PATH_NODE_PROPERTY)
    @SerializeWith(PropertyValueSerializationStrategy.class)
    public void getNodeProperty(Invocation invocation, Output result)
    {
        Node node = invocation.getDB().getNodeById(getNodeId(invocation));
        result.ok(node.getProperty(invocation.getStringParameter(NODE_PROPERTY_KEY_NAME)));
    }

    @DELETE
    @Transactional
    @Path(PATH_NODE_PROPERTY)
    public void deleteNodeProperty(Invocation invocation, Output result)
    {
        Node node = invocation.getDB().getNodeById(getNodeId(invocation));
        node.removeProperty(invocation.getStringParameter(NODE_PROPERTY_KEY_NAME));
        result.ok();
    }

    @DELETE
    @Transactional
    @Path(PATH_NODE_PROPERTIES)
    public void deleteAllNodeProperties(Invocation invocation, Output result)
    {
        Node node = invocation.getDB().getNodeById(getNodeId(invocation));
        removeAllProperties(node);
        result.ok();
    }

    private long getNodeId(Invocation invocation)
    {
        return invocation.getLongParameter(NODE_ID_NAME, -1l);
    }

    private void setProperties(PropertyContainer entity, Map<String, Object> content)
    {
        for(Map.Entry<String,Object> entry : content.entrySet()) 
        {
            entity.setProperty(entry.getKey(), entry.getValue());
        }
    }

    private void removeAllProperties(Node node)
    {
        for(String key: node.getPropertyKeys()) 
        {
            node.removeProperty(key);
        }
    }
}