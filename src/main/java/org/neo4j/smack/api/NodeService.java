package org.neo4j.smack.api;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.neo4j.server.rest.repr.NodeRepresentation;
import org.neo4j.server.rest.web.NoSuchPropertyException;
import org.neo4j.server.rest.web.NodeNotFoundException;
import org.neo4j.server.rest.web.OperationFailureException;
import org.neo4j.server.rest.web.PropertyValueException;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.annotation.Transactional;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Output;
import org.neo4j.smack.serialization.strategy.PropertyMapDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.RepresentationSerializationStrategy;
import org.neo4j.smack.serialization.strategy.ValueDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.ValueOrNullDeserializationStrategy;

/**
 * @author mh
 * @since 05.12.11
 */
public class NodeService extends RestService {

    private static final String PATH_NODE_PROPERTIES = PATH_NODE + "/properties";
    private static final String PATH_NODE_PROPERTY = PATH_NODE_PROPERTIES + "/{key}";

    public NodeService(String dataPath) {
        super(dataPath);
    }

    @GET
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getRoot(Invocation invocation, Output result) {
        result.ok(actionsFor(invocation).root());
    }

    @POST
    @Transactional
    @Path(PATH_NODES)
    @DeserializeWith(ValueOrNullDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void createNode(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException {
        Object payload = invocation.getDeserializedContent();
        if (payload==null) payload = Collections.emptyMap();
        final NodeRepresentation node = actionsFor(invocation).createNode((Map<String, Object>) payload);
        final String location = createOutputFormat(invocation).format(node.selfUri());
        result.createdAt(location, node);
    }


    @GET
    @Path(PATH_NODE)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getNode(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException {
        final Long nodeId = getNodeId(invocation);
        result.ok(actionsFor(invocation).getNode(nodeId));
    }

    @DELETE
    @Path(PATH_NODE)
    public void deleteNode(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        actionsFor(invocation).deleteNode(getNodeId(invocation));
        result.ok();
    }

    @PUT
    @Path(PATH_NODE_PROPERTIES)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    public void setAllNodeProperties(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        actionsFor(invocation).setAllNodeProperties(getNodeId(invocation), (Map<String, Object>) invocation.getDeserializedContent());
        result.okNoContent();
    }

    @GET
    @Path(PATH_NODE_PROPERTIES)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getAllNodeProperties(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        result.ok(actionsFor(invocation).getAllNodeProperties(getNodeId(invocation)));
    }

    @PUT
    @Path(PATH_NODE_PROPERTY)
    @DeserializeWith(ValueDeserializationStrategy.class)
    public void setNodeProperty(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException {
        actionsFor(invocation).setNodeProperty(getNodeId(invocation), getKey(invocation), invocation.getDeserializedContent());
        result.okNoContent();
    }

    @GET
    @Path(PATH_NODE_PROPERTY)
    public void getNodeProperty(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException, NoSuchPropertyException {
        result.ok(actionsFor(invocation).getNodeProperty(getNodeId(invocation), getKey(invocation)));
    }

    @DELETE
    @Path(PATH_NODE_PROPERTY)
    public void deleteNodeProperty(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException, NoSuchPropertyException {
        actionsFor(invocation).removeNodeProperty(getNodeId(invocation), getKey(invocation));
        result.ok();
    }

    @DELETE
    @Path(PATH_NODE_PROPERTIES)
    public void deleteAllNodeProperties(Invocation invocation, Output result) throws PropertyValueException, URISyntaxException, NodeNotFoundException, OperationFailureException, NoSuchPropertyException {
        actionsFor(invocation).removeAllNodeProperties(getNodeId(invocation));
        result.ok();
    }
}