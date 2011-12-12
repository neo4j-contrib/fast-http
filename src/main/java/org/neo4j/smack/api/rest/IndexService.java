package org.neo4j.smack.api.rest;

import org.neo4j.server.rest.repr.IndexedEntityRepresentation;
import org.neo4j.server.rest.web.DatabaseActions;
import org.neo4j.server.smack.serialization.RepresentationSerializationStrategy;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Result;
import org.neo4j.smack.serialization.strategy.PropertyMapDeserializationStrategy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author mh
 * @since 05.12.11
 */
public class IndexService extends RestService {

    protected static final String PATH_NODE_INDEX = "index/node";
    protected static final String PATH_NAMED_NODE_INDEX = PATH_NODE_INDEX + "/{indexName}";
    protected static final String PATH_NODE_INDEX_GET = PATH_NAMED_NODE_INDEX + "/{key}/{value}";
    protected static final String PATH_NODE_INDEX_QUERY_WITH_KEY = PATH_NAMED_NODE_INDEX + "/{key}"; // http://localhost/db/data/index/node/foo?query=somelucenestuff
    protected static final String PATH_NODE_INDEX_ID = PATH_NODE_INDEX_GET + "/{id}";
    protected static final String PATH_NODE_INDEX_REMOVE_KEY = PATH_NAMED_NODE_INDEX + "/{key}/{id}";
    protected static final String PATH_NODE_INDEX_REMOVE = PATH_NAMED_NODE_INDEX + "/{id}";
    protected static final String PATH_RELATIONSHIP_INDEX = "index/relationship";
    protected static final String PATH_NAMED_RELATIONSHIP_INDEX = PATH_RELATIONSHIP_INDEX + "/{indexName}";
    protected static final String PATH_RELATIONSHIP_INDEX_GET = PATH_NAMED_RELATIONSHIP_INDEX + "/{key}/{value}";
    protected static final String PATH_RELATIONSHIP_INDEX_QUERY_WITH_KEY = PATH_NAMED_RELATIONSHIP_INDEX + "/{key}";
    protected static final String PATH_RELATIONSHIP_INDEX_ID = PATH_RELATIONSHIP_INDEX_GET + "/{id}";
    protected static final String PATH_RELATIONSHIP_INDEX_REMOVE_KEY = PATH_NAMED_RELATIONSHIP_INDEX + "/{key}/{id}";
    protected static final String PATH_RELATIONSHIP_INDEX_REMOVE = PATH_NAMED_RELATIONSHIP_INDEX + "/{id}";
    public static final String PATH_AUTO_NODE_INDEX = "index/auto/node";
    protected static final String PATH_AUTO_NODE_INDEX_GET = PATH_AUTO_NODE_INDEX + "/{key}/{value}";
    public static final String PATH_AUTO_RELATIONSHIP_INDEX = "index/auto/relationship";
    protected static final String PATH_AUTO_RELATIONSHIP_INDEX_GET = PATH_AUTO_RELATIONSHIP_INDEX + "/{key}/{value}";

    public IndexService(String baseUri, String dataPath) {
        super(baseUri,dataPath);
    }

    @GET
    @Path(PATH_NODE_INDEX)
    public void getNodeIndexRoot(Invocation invocation, Result result) throws Exception {
        final DatabaseActions actions = actionsFor(invocation);
        if (actions.getNodeIndexNames().length > 0) {
            result.setData(actions.nodeIndexRoot());
        }
        result.setOk();
    }

    @POST
    @Path(PATH_NODE_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void jsonCreateNodeIndex(Invocation invocation, Result result) throws Exception {
        result.setCreated();
        result.setData(actionsFor(invocation).createNodeIndex(readMap(invocation)));
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX)
    public void getRelationshipIndexRoot(Invocation invocation, Result result) throws Exception {
        final DatabaseActions actions = actionsFor(invocation);
        if (actions.getRelationshipIndexNames().length > 0) {
            result.setData(actions.relationshipIndexRoot());
        }
        result.setOk();
    }

    @POST
    @Path(PATH_RELATIONSHIP_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void jsonCreateRelationshipIndex(Invocation invocation, Result result) throws Exception {
        result.setData(actionsFor(invocation).createRelationshipIndex(readMap(invocation)));
        result.setCreated();
    }

    @GET
    @Path(PATH_NAMED_NODE_INDEX)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedNodesByQuery(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedNodesByQuery(getIndexName(invocation), getQueryString(invocation)));
    }

    private String getIndexName(Invocation invocation) {
        return getParameter(invocation, "indexName");
    }

    @GET
    @Path(PATH_AUTO_NODE_INDEX)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedNodesByQuery(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getAutoIndexedNodesByQuery(getQueryString(invocation)));
    }

    @DELETE
    @Path(PATH_NAMED_NODE_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteNodeIndex(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeNodeIndex(getIndexName(invocation));
        result.setOk();
    }

    @DELETE
    @Path(PATH_NAMED_RELATIONSHIP_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteRelationshipIndex(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeRelationshipIndex(getIndexName(invocation));
        result.setOk();
    }

    @POST
    @Path(PATH_NAMED_NODE_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void addToNodeIndex(Invocation invocation, Result result) throws Exception {
        Map<String, Object> entityBody = readMap(invocation);
        final IndexedEntityRepresentation addToIndexResponse = actionsFor(invocation).addToNodeIndex(getIndexName(invocation), (String) entityBody.get("key"), (String) entityBody.get("value"), extractId(entityBody.get("uri")));
        result.setCreated();
        result.setData(addToIndexResponse);
    }

    @POST
    @Path(PATH_NAMED_RELATIONSHIP_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void addToRelationshipIndex(Invocation invocation, Result result) throws Exception {
        Map<String, Object> entityBody = readMap(invocation);
        final IndexedEntityRepresentation addToIndexResponse = actionsFor(invocation).addToRelationshipIndex(getIndexName(invocation), (String) entityBody.get("key"), (String) entityBody.get("value"), extractId(entityBody.get("uri")));
        result.setCreated();
        result.setData(addToIndexResponse);
    }

    @GET
    @Path(PATH_NODE_INDEX_ID)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getNodeFromIndexUri(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedNode(getIndexName(invocation), getKey(invocation), getValue(invocation), getId(invocation)));
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX_ID)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getRelationshipFromIndexUri(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedRelationship(getIndexName(invocation), getKey(invocation), getValue(invocation), getId(invocation)));
    }

    @GET
    @Path(PATH_NODE_INDEX_GET)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedNodes(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedNodes(getIndexName(invocation), getKey(invocation), getValue(invocation)));


    }

    private String getValue(Invocation invocation) {
        return getParameter(invocation, "value");
    }

    @GET
    @Path(PATH_AUTO_NODE_INDEX_GET)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedNodes(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getAutoIndexedNodes(getKey(invocation), getValue(invocation)));
    }

    @GET
    @Path(PATH_NODE_INDEX_QUERY_WITH_KEY)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedNodesByQueryWithKey(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedNodesByQuery(getIndexName(invocation), getKey(invocation), getQueryString(invocation)));
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX_GET)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedRelationships(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedRelationships(getIndexName(invocation), getKey(invocation), getValue(invocation)));
    }

    @GET
    @Path(PATH_AUTO_RELATIONSHIP_INDEX_GET)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedRelationships(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getAutoIndexedRelationships(getKey(invocation), getValue(invocation)));
    }

    @GET
    @Path(PATH_AUTO_RELATIONSHIP_INDEX)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedRelationshipsByQuery(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getAutoIndexedRelationshipsByQuery(getQueryString(invocation)));
    }

    @GET
    @Path(PATH_NAMED_RELATIONSHIP_INDEX)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getNameIndexedRelationshipsByQuery(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedRelationshipsByQuery(getIndexName(invocation), getQueryString(invocation)));
    }

    private String getQueryString(Invocation invocation) {
        return getParameter(invocation, "query");
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX_QUERY_WITH_KEY)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedRelationshipsByQuery(Invocation invocation, Result result) throws Exception {
        result.setOk(actionsFor(invocation).getIndexedRelationshipsByQuery(getIndexName(invocation), getKey(invocation), getQueryString(invocation)));
    }

    @DELETE
    @Path(PATH_NODE_INDEX_ID)
    public void deleteFromNodeIndex(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeFromNodeIndex(getIndexName(invocation), getKey(invocation), getValue(invocation), getId(invocation));
        result.setOk();
    }

    @DELETE
    @Path(PATH_NODE_INDEX_REMOVE_KEY)
    public void deleteFromNodeIndexNoValue(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeFromNodeIndexNoValue(getIndexName(invocation), getKey(invocation), getId(invocation));
        result.setOk();
    }

    @DELETE
    @Path(PATH_NODE_INDEX_REMOVE)
    public void deleteFromNodeIndexNoKeyValue(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeFromNodeIndexNoKeyValue(getIndexName(invocation), getId(invocation));
        result.setOk();
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_INDEX_ID)
    public void deleteFromRelationshipIndex(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeFromRelationshipIndex(getIndexName(invocation), getKey(invocation), getValue(invocation), getId(invocation));
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_INDEX_REMOVE_KEY)
    public void deleteFromRelationshipIndexNoValue(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removeFromRelationshipIndexNoValue(getIndexName(invocation), getKey(invocation), getId(invocation));
        result.setOk();
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_INDEX_REMOVE)
    public void deleteFromRelationshipIndexWithId(Invocation invocation, Result result) throws Exception { // @PathParam("indexName") String indexName, @PathParam("value") String value, @PathParam("id") long id
        actionsFor(invocation).removeFromRelationshipIndexNoKeyValue(getIndexName(invocation), getId(invocation));
        result.setOk();
    }
}