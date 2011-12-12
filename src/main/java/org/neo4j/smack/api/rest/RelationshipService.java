package org.neo4j.smack.api.rest;

import org.neo4j.server.rest.domain.EndNodeNotFoundException;
import org.neo4j.server.rest.domain.StartNodeNotFoundException;
import org.neo4j.server.rest.repr.BadInputException;
import org.neo4j.server.rest.repr.PropertiesRepresentation;
import org.neo4j.server.rest.repr.RelationshipRepresentation;
import org.neo4j.server.rest.web.DatabaseActions;
import org.neo4j.server.rest.web.NoSuchPropertyException;
import org.neo4j.server.rest.web.NodeNotFoundException;
import org.neo4j.server.rest.web.OperationFailureException;
import org.neo4j.server.smack.serialization.RepresentationSerializationStrategy;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Result;
import org.neo4j.smack.serialization.strategy.PropertyMapDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.ValueDeserializationStrategy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

/**
 * @author mh
 * @since 05.12.11
 */
public class RelationshipService extends RestService {
    private static final String PATH_NODE_RELATIONSHIPS_W_DIR = PATH_NODE_RELATIONSHIPS + "/{direction}";
    private static final String PATH_NODE_RELATIONSHIPS_W_DIR_N_TYPES = PATH_NODE_RELATIONSHIPS_W_DIR + "/{types}";
    private static final String PATH_RELATIONSHIP_PROPERTIES = PATH_RELATIONSHIP + "/properties";
    private static final String PATH_RELATIONSHIP_PROPERTY = PATH_RELATIONSHIP_PROPERTIES + "/{key}";

    public RelationshipService(String baseUri, String dataPath) {
        super(baseUri,dataPath);
    }


    @SuppressWarnings("unchecked")
    @POST
    @Path(PATH_NODE_RELATIONSHIPS)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void createRelationship(Invocation invocation, Result result) throws BadInputException, URISyntaxException, NodeNotFoundException, OperationFailureException, NoSuchPropertyException, StartNodeNotFoundException, EndNodeNotFoundException {
        final Map<String, Object> data = invocation.getDeserializedContent();
        final long endNodeId = extractId(data.get("to"));
        final String type = (String) data.get("type");
        final Map<String, Object> properties = (Map<String, Object>) data.get("data");
        final RelationshipRepresentation relationship = actionsFor(invocation).createRelationship(getNodeId(invocation), endNodeId, type, properties);
        final String location = createOutputFormat(invocation).format(relationship.selfUri());
        result.setCreated(location);
        result.setData(relationship);
    }

    @GET
    @Path(PATH_RELATIONSHIP)
    public void getRelationship(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getRelationship(getRelationshipId(invocation)));
    }

    @DELETE
    @Path(PATH_RELATIONSHIP)
    public void deleteRelationship(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).deleteRelationship(getRelationshipId(invocation));
        result.setOk();
    }

    @GET
    @Path(PATH_NODE_RELATIONSHIPS_W_DIR)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getNodeRelationships(Invocation invocation, Result result) throws Exception {
        final DatabaseActions.RelationshipDirection direction = DatabaseActions.RelationshipDirection.valueOf(getParameter(invocation, "direction"));
        result.setOk(actionsFor(invocation).getNodeRelationships(getNodeId(invocation), direction, Collections.<String>emptyList()));
    }

    @GET
    @Path(PATH_NODE_RELATIONSHIPS_W_DIR_N_TYPES)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getNodeRelationshipsWithDirAndTypes(Invocation invocation, Result result) throws Exception {
        final DatabaseActions.RelationshipDirection direction = DatabaseActions.RelationshipDirection.valueOf(getParameter(invocation, "direction"));
        final AmpersandSeparatedCollection types = new AmpersandSeparatedCollection(getParameter(invocation, "types"));
        result.setOk(actionsFor(invocation).getNodeRelationships(getNodeId(invocation), direction, types));
    }

    @GET
    @Path(PATH_RELATIONSHIP_PROPERTIES)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getAllRelationshipProperties(Invocation invocation, Result result) throws Exception {
        final PropertiesRepresentation properties = actionsFor(invocation).getAllRelationshipProperties(getRelationshipId(invocation));
        if (!properties.isEmpty()) result.setData(properties);
        result.setOk();
    }

    @GET
    @Path(PATH_RELATIONSHIP_PROPERTY)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getRelationshipProperty(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getRelationshipProperty(getRelationshipId(invocation), getKey(invocation)));
    }

    @PUT
    @Path(PATH_RELATIONSHIP_PROPERTIES)
    @Consumes(MediaType.APPLICATION_JSON)
    public void setAllRelationshipProperties(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).setAllRelationshipProperties(getRelationshipId(invocation), readMap(invocation));
        result.setOk();
    }

    @PUT
    @Path(PATH_RELATIONSHIP_PROPERTY)
    @Consumes(MediaType.APPLICATION_JSON)
    @DeserializeWith(ValueDeserializationStrategy.class)
    public void setRelationshipProperty(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).setRelationshipProperty(getRelationshipId(invocation), getKey(invocation), invocation.getDeserializedContent());
        result.setOk();
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_PROPERTIES)
    public void deleteAllRelationshipProperties(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeAllRelationshipProperties(getRelationshipId(invocation));
        result.setOk();
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_PROPERTY)
    public void deleteRelationshipProperty(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeRelationshipProperty(getRelationshipId(invocation), getKey(invocation));
        result.setOk();
    }
}