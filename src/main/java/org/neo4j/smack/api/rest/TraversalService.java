package org.neo4j.smack.api.rest;

import org.neo4j.server.rest.domain.TraverserReturnType;
import org.neo4j.server.rest.repr.BadInputException;
import org.neo4j.server.rest.repr.ListRepresentation;
import org.neo4j.server.rest.web.DatabaseActions;
import org.neo4j.server.smack.serialization.RepresentationSerializationStrategy;
import org.neo4j.smack.annotation.DeserializeWith;
import org.neo4j.smack.annotation.SerializeWith;
import org.neo4j.smack.event.Invocation;
import org.neo4j.smack.event.Result;
import org.neo4j.smack.serialization.strategy.PropertyMapDeserializationStrategy;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Map;

/**
 * @author mh
 * @since 05.12.11
 */
public class TraversalService extends RestService {
    private static final String PATH_NODE_TRAVERSE = PATH_NODE + "/traverse/{returnType}";
    private static final String PATH_NODE_PATH = PATH_NODE + "/path";
    private static final String PATH_NODE_PATHS = PATH_NODE + "/paths";
    public static final String PATH_TO_CREATE_PAGED_TRAVERSERS = PATH_NODE + "/paged/traverse/{returnType}";
    public static final String PATH_TO_PAGED_TRAVERSERS = PATH_NODE + "/paged/traverse/{returnType}/{traverserId}";

    public TraversalService(String baseUri, String dataPath) {
        super(baseUri,dataPath);
    }

    @POST
    @Path(PATH_NODE_TRAVERSE)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void traverse(Invocation invocation, Result result) throws Exception {
        final TraverserReturnType returnType = getReturnType(invocation);
        result.setOk(actionsFor(invocation).traverse(getNodeId(invocation), readMap(invocation), returnType));
    }
    @DELETE
    @Path(PATH_TO_PAGED_TRAVERSERS)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    public void removePagedTraverser(Invocation invocation, Result result) throws Exception {
        actionsFor(invocation).removePagedTraverse(getParameter(invocation, "traverserId"));
        result.setOk();
    }

    @GET
    @Path(PATH_TO_PAGED_TRAVERSERS)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void pagedTraverse(Invocation invocation, Result result) throws Exception {
        final TraverserReturnType returnType = getReturnType(invocation);
        ListRepresentation traversalResult = actionsFor(invocation).pagedTraverse(getParameter(invocation, "traverserId"), returnType);
        result.setOk(traversalResult);
        result.setLocation(invocation.getPath()); // todo only relative location
    }

    private TraverserReturnType getReturnType(Invocation invocation) {
        final String value = getParameter(invocation, "returnType");
        if (value==null) return TraverserReturnType.node;
        return TraverserReturnType.valueOf(value);
    }

    @POST
    @Path(PATH_TO_CREATE_PAGED_TRAVERSERS)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void createPagedTraverser(Invocation invocation, Result result) throws Exception {
        final Long pageSize = getLongParameter(invocation, "pageSize", 50L);
        validatePageSize(pageSize);
        final Long leaseTimeInSeconds = getLongParameter(invocation, "leaseTime", 60L);
        validateLeaseTime(leaseTimeInSeconds);
        final DatabaseActions actions = actionsFor(invocation);
        String traverserId = actions.createPagedTraverser(getNodeId(invocation), readMap(invocation), pageSize.intValue(), leaseTimeInSeconds.intValue());
        final ListRepresentation pagedTraverseResult = actions.pagedTraverse(traverserId, getReturnType(invocation));
        result.setData(pagedTraverseResult);
        result.setLocation(invocation.getPath()); // todo only relative location !
        result.setOk();
    }


    private void validateLeaseTime(Long leaseTimeInSeconds) throws BadInputException {
        if (leaseTimeInSeconds < 1)
            throw new BadInputException("Lease time less than 1 second is not supported");
    }

    private void validatePageSize(Long pageSize) throws BadInputException {
        if (pageSize < 1)
            throw new BadInputException("Page size less than 1 is not permitted");
    }

    @POST
    @Path(PATH_NODE_PATH)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void singlePath(Invocation invocation, Result result) throws Exception { // @PathParam("getNodeId(invocation)") long startNode, String body
        final Map<String, Object> data = readMap(invocation);
        final long endNode = extractId(data.get("to"));
        result.setOk(actionsFor(invocation).findSinglePath(getNodeId(invocation), endNode, data));
    }

    @POST
    @Path(PATH_NODE_PATHS)
    @DeserializeWith(PropertyMapDeserializationStrategy.class)
    @SerializeWith(RepresentationSerializationStrategy.class)
    public void allPaths(Invocation invocation, Result result) throws Exception { // @PathParam("getNodeId(invocation)") long startNode, String body
        final Map<String, Object> description = readMap(invocation);
        final long endNode = extractId(description.get("to"));
        result.setOk(actionsFor(invocation).findPaths(getNodeId(invocation), endNode, description));
    }
}