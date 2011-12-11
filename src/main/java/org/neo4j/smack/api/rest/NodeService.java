package org.neo4j.smack.api.rest;

import org.neo4j.server.rest.repr.NodeRepresentation;
import org.neo4j.server.rest.web.NoSuchPropertyException;
import org.neo4j.server.rest.web.NodeNotFoundException;
import org.neo4j.server.rest.web.OperationFailureException;
import org.neo4j.server.rest.web.PropertyValueException;
import org.neo4j.server.smack.serialization.RepresentationSerializationStrategy;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.annotation.Transactional;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Result;
import org.neo4j.smack.serialization.strategy.PropertyMapDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.ValueDeserializationStrategy;

import javax.ws.rs.*;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author mh
 * @since 05.12.11
 */
public class NodeService extends RestService {

    private static final String PATH_NODE_PROPERTIES = PATH_NODE + "/properties";
    private static final String PATH_NODE_PROPERTY = PATH_NODE_PROPERTIES + "/{key}";

    public NodeService() {
    }


    @GET
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getRoot(Invocation invocation, Result result) {
        result.setOk(actionsFor(invocation).root());
    }

    @POST
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    @Transactional
    @Path(PATH_NODES)
    public void createNode(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException {
        final NodeRepresentation node = actionsFor(invocation).createNode((Map<String, Object>) invocation.getDeserializedContent());
        final String location = createOutputFormat(invocation).format(node.selfUri());
        result.setCreated(location);
        result.setData(node);
    }


    @GET
    @Path(PATH_NODE)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getNode(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException {
        final Long nodeId = getNodeId(invocation);
        result.setOk(actionsFor(invocation).getNode(nodeId));
    }

    @DELETE
    @Path(PATH_NODE)
    public void deleteNode(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        actionsFor(invocation).deleteNode(getNodeId(invocation));
        result.setOk();
    }


    @PUT
    @Path(PATH_NODE_PROPERTIES)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    public void setAllNodeProperties(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        actionsFor(invocation).setAllNodeProperties(getNodeId(invocation), (Map<String, Object>) invocation.getDeserializedContent());
        result.setOk();
    }


    @GET
    @Path(PATH_NODE_PROPERTIES)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getAllNodeProperties(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        result.setOk(actionsFor(invocation).getAllNodeProperties(getNodeId(invocation)));
    }

    @PUT
    @Path(PATH_NODE_PROPERTY)
    @DeserializeWith(ValueDeserializationStrategy.class)
    public void setNodeProperty(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        actionsFor(invocation).setNodeProperty(getNodeId(invocation), getKey(invocation), invocation.getDeserializedContent());
        result.setOk();
    }

    @GET
    @Path(PATH_NODE_PROPERTY)
    public void getNodeProperty(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException, NoSuchPropertyException {
        result.setOk(actionsFor(invocation).getNodeProperty(getNodeId(invocation), getKey(invocation)));
    }

    @DELETE
    @Path(PATH_NODE_PROPERTY)
    public void deleteNodeProperty(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException, NoSuchPropertyException {
        actionsFor(invocation).removeNodeProperty(getNodeId(invocation), getKey(invocation));
        result.setOk();
    }

    @DELETE
    @Path(PATH_NODE_PROPERTIES)
    public void deleteAllNodeProperties(Invocation invocation, Result result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException, NoSuchPropertyException {
        actionsFor(invocation).removeAllNodeProperties(getNodeId(invocation));
        result.setOk();
    }
}