package org.neo4j.smack.api;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.neo4j.server.rest.repr.IndexedEntityRepresentation;
import org.neo4j.server.rest.web.DatabaseActions;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Output;
import org.neo4j.smack.serialization.strategy.PropertyMapDeserializationStrategy;

/**
 * @author mh
 * @since 05.12.11
 */
public class IndexService extends RestService {

    protected static final String PATH_NODE_INDEX = "index/node";
    protected static final String PATH_NAMED_NODE_INDEX = PATH_NODE_INDEX
            + "/{indexName}";
    protected static final String PATH_NODE_INDEX_GET = PATH_NAMED_NODE_INDEX
            + "/{key}/{value}";
    protected static final String PATH_NODE_INDEX_QUERY_WITH_KEY = PATH_NAMED_NODE_INDEX
            + "/{key}"; // http://localhost/db/data/index/node/foo?query=somelucenestuff
    protected static final String PATH_NODE_INDEX_ID = PATH_NODE_INDEX_GET
            + "/{id}";
    protected static final String PATH_NODE_INDEX_REMOVE_KEY = PATH_NAMED_NODE_INDEX
            + "/{key}/{id}";
    protected static final String PATH_NODE_INDEX_REMOVE = PATH_NAMED_NODE_INDEX
            + "/{id}";
    protected static final String PATH_RELATIONSHIP_INDEX = "index/relationship";
    protected static final String PATH_NAMED_RELATIONSHIP_INDEX = PATH_RELATIONSHIP_INDEX
            + "/{indexName}";
    protected static final String PATH_RELATIONSHIP_INDEX_GET = PATH_NAMED_RELATIONSHIP_INDEX
            + "/{key}/{value}";
    protected static final String PATH_RELATIONSHIP_INDEX_QUERY_WITH_KEY = PATH_NAMED_RELATIONSHIP_INDEX
            + "/{key}";
    protected static final String PATH_RELATIONSHIP_INDEX_ID = PATH_RELATIONSHIP_INDEX_GET
            + "/{id}";
    protected static final String PATH_RELATIONSHIP_INDEX_REMOVE_KEY = PATH_NAMED_RELATIONSHIP_INDEX
            + "/{key}/{id}";
    protected static final String PATH_RELATIONSHIP_INDEX_REMOVE = PATH_NAMED_RELATIONSHIP_INDEX
            + "/{id}";
    public static final String PATH_AUTO_NODE_INDEX = "index/auto/node";
    protected static final String PATH_AUTO_NODE_INDEX_GET = PATH_AUTO_NODE_INDEX
            + "/{key}/{value}";
    public static final String PATH_AUTO_RELATIONSHIP_INDEX = "index/auto/relationship";
    protected static final String PATH_AUTO_RELATIONSHIP_INDEX_GET = PATH_AUTO_RELATIONSHIP_INDEX
            + "/{key}/{value}";

    public IndexService(String dataPath)
    {
        super(dataPath);
    }

    @GET
    @Path(PATH_NODE_INDEX)
    public void getNodeIndexRoot(Invocation invocation, Output result)
            throws Exception
    {
        final DatabaseActions actions = actionsFor(invocation);
        if (actions.getNodeIndexNames().length > 0)
        {
            result.ok(actions.nodeIndexRoot());
        } else
        {
            result.ok();
        }

    }

    @POST
    @Path(PATH_NODE_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void jsonCreateNodeIndex(Invocation invocation, Output result)
            throws Exception
    {
        result.created(actionsFor(invocation).createNodeIndex(
                readMap(invocation)));
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX)
    public void getRelationshipIndexRoot(Invocation invocation, Output result)
            throws Exception
    {
        final DatabaseActions actions = actionsFor(invocation);
        if (actions.getRelationshipIndexNames().length > 0)
        {
            result.ok(actions.relationshipIndexRoot());
        } else {
            result.ok();
        }
    }

    @POST
    @Path(PATH_RELATIONSHIP_INDEX)
    @Consumes(MediaType.APPLICATION_JSON)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void jsonCreateRelationshipIndex(Invocation invocation, Output result)
            throws Exception
    {
        result.created(actionsFor(invocation).createRelationshipIndex(
                readMap(invocation)));
    }

    @GET
    @Path(PATH_NAMED_NODE_INDEX)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedNodesByQuery(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedNodesByQuery(
                getIndexName(invocation), getQueryString(invocation)));
    }

    private String getIndexName(Invocation invocation)
    {
        return getParameter(invocation, "indexName");
    }

    @GET
    @Path(PATH_AUTO_NODE_INDEX)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedNodesByQuery(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getAutoIndexedNodesByQuery(
                getQueryString(invocation)));
    }

    @DELETE
    @Path(PATH_NAMED_NODE_INDEX)
    public void deleteNodeIndex(Invocation invocation, Output result)
            throws Exception
    {
        actionsFor(invocation).removeNodeIndex(getIndexName(invocation));
        result.ok();
    }

    @DELETE
    @Path(PATH_NAMED_RELATIONSHIP_INDEX)
    public void deleteRelationshipIndex(Invocation invocation, Output result)
            throws Exception
    {
        actionsFor(invocation)
                .removeRelationshipIndex(getIndexName(invocation));
        result.ok();
    }

    @POST
    @Path(PATH_NAMED_NODE_INDEX)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void addToNodeIndex(Invocation invocation, Output result)
            throws Exception
    {
        Map<String, Object> entityBody = readMap(invocation);
        final IndexedEntityRepresentation addToIndexResponse = actionsFor(
                invocation).addToNodeIndex(getIndexName(invocation),
                (String) entityBody.get("key"),
                (String) entityBody.get("value"),
                extractId(entityBody.get("uri")));
        result.created(addToIndexResponse);
    }

    @POST
    @Path(PATH_NAMED_RELATIONSHIP_INDEX)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void addToRelationshipIndex(Invocation invocation, Output result)
            throws Exception
    {
        Map<String, Object> entityBody = readMap(invocation);
        final IndexedEntityRepresentation addToIndexResponse = actionsFor(
                invocation).addToRelationshipIndex(getIndexName(invocation),
                (String) entityBody.get("key"),
                (String) entityBody.get("value"),
                extractId(entityBody.get("uri")));
        result.created(addToIndexResponse);
    }

    @GET
    @Path(PATH_NODE_INDEX_ID)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getNodeFromIndexUri(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedNode(
                getIndexName(invocation), getKey(invocation),
                getValue(invocation), getId(invocation)));
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX_ID)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getRelationshipFromIndexUri(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedRelationship(
                getIndexName(invocation), getKey(invocation),
                getValue(invocation), getId(invocation)));
    }

    @GET
    @Path(PATH_NODE_INDEX_GET)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedNodes(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedNodes(
                getIndexName(invocation), getKey(invocation),
                getValue(invocation)));

    }

    private String getValue(Invocation invocation)
    {
        return getParameter(invocation, "value");
    }

    @GET
    @Path(PATH_AUTO_NODE_INDEX_GET)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedNodes(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getAutoIndexedNodes(
                getKey(invocation), getValue(invocation)));
    }

    @GET
    @Path(PATH_NODE_INDEX_QUERY_WITH_KEY)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedNodesByQueryWithKey(Invocation invocation,
            Output result) throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedNodesByQuery(
                getIndexName(invocation), getKey(invocation),
                getQueryString(invocation)));
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX_GET)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedRelationships(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedRelationships(
                getIndexName(invocation), getKey(invocation),
                getValue(invocation)));
    }

    @GET
    @Path(PATH_AUTO_RELATIONSHIP_INDEX_GET)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedRelationships(Invocation invocation, Output result)
            throws Exception
    {
        result.ok(actionsFor(invocation).getAutoIndexedRelationships(
                getKey(invocation), getValue(invocation)));
    }

    @GET
    @Path(PATH_AUTO_RELATIONSHIP_INDEX)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getAutoIndexedRelationshipsByQuery(Invocation invocation,
            Output result) throws Exception
    {
        result.ok(actionsFor(invocation).getAutoIndexedRelationshipsByQuery(
                getQueryString(invocation)));
    }

    @GET
    @Path(PATH_NAMED_RELATIONSHIP_INDEX)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getNameIndexedRelationshipsByQuery(Invocation invocation,
            Output result) throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedRelationshipsByQuery(
                getIndexName(invocation), getQueryString(invocation)));
    }

    private String getQueryString(Invocation invocation)
    {
        return getParameter(invocation, "query");
    }

    @GET
    @Path(PATH_RELATIONSHIP_INDEX_QUERY_WITH_KEY)
    //@SerializeWith(RepresentationSerializationStrategy.class)
    public void getIndexedRelationshipsByQuery(Invocation invocation,
            Output result) throws Exception
    {
        result.ok(actionsFor(invocation).getIndexedRelationshipsByQuery(
                getIndexName(invocation), getKey(invocation),
                getQueryString(invocation)));
    }

    @DELETE
    @Path(PATH_NODE_INDEX_ID)
    public void deleteFromNodeIndex(Invocation invocation, Output result)
            throws Exception
    {
        actionsFor(invocation).removeFromNodeIndex(getIndexName(invocation),
                getKey(invocation), getValue(invocation), getId(invocation));
        result.ok();
    }

    @DELETE
    @Path(PATH_NODE_INDEX_REMOVE_KEY)
    public void deleteFromNodeIndexNoValue(Invocation invocation, Output result)
            throws Exception
    {
        actionsFor(invocation)
                .removeFromNodeIndexNoValue(getIndexName(invocation),
                        getKey(invocation), getId(invocation));
        result.ok();
    }

    @DELETE
    @Path(PATH_NODE_INDEX_REMOVE)
    public void deleteFromNodeIndexNoKeyValue(Invocation invocation,
            Output result) throws Exception
    {
        actionsFor(invocation).removeFromNodeIndexNoKeyValue(
                getIndexName(invocation), getId(invocation));
        result.ok();
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_INDEX_ID)
    public void deleteFromRelationshipIndex(Invocation invocation, Output result)
            throws Exception
    {
        actionsFor(invocation).removeFromRelationshipIndex(
                getIndexName(invocation), getKey(invocation),
                getValue(invocation), getId(invocation));
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_INDEX_REMOVE_KEY)
    public void deleteFromRelationshipIndexNoValue(Invocation invocation,
            Output result) throws Exception
    {
        actionsFor(invocation)
                .removeFromRelationshipIndexNoValue(getIndexName(invocation),
                        getKey(invocation), getId(invocation));
        result.ok();
    }

    @DELETE
    @Path(PATH_RELATIONSHIP_INDEX_REMOVE)
    public void deleteFromRelationshipIndexWithId(Invocation invocation,
            Output result) throws Exception
    { // @PathParam("indexName") String indexName, @PathParam("value") String
      // value, @PathParam("id") long id
        actionsFor(invocation).removeFromRelationshipIndexNoKeyValue(
                getIndexName(invocation), getId(invocation));
        result.ok();
    }
}