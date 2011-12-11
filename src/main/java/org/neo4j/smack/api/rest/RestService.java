package org.neo4j.smack.api.rest;

import org.neo4j.server.rest.paging.LeaseManager;
import org.neo4j.server.rest.paging.RealClock;
import org.neo4j.server.rest.repr.BadInputException;
import org.neo4j.server.rest.repr.ExtensionInjector;
import org.neo4j.server.rest.repr.OutputFormat;
import org.neo4j.server.rest.repr.RepresentationFormatRepository;
import org.neo4j.server.rest.web.DatabaseActions;
import org.neo4j.smack.event.Invocation;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author mh
 * @since 11.12.11
 */
public class RestService {
    private static final LeaseManager leaseManager = new LeaseManager(new RealClock());
    public static final String NODE_ID_NAME = "nodeId";
    protected static final String PATH_NODES = "node";
    protected static final String PATH_NODE = PATH_NODES + "/{" + NODE_ID_NAME + "}";
    public static final String RELATIONSHIP_ID_NAME = "relationshipId";
    protected static final String PATH_NODE_RELATIONSHIPS = PATH_NODE + "/relationships";
    protected static final String PATH_RELATIONSHIP = "relationship/{" + RELATIONSHIP_ID_NAME + "}";
    RepresentationFormatRepository repository = new RepresentationFormatRepository(new ExtensionInjector() {
        @Override
        public Map<String, List<String>> getExensionsFor(Class<?> aClass) {
            return Collections.emptyMap();
        }
    });

    protected DatabaseActions actionsFor(Invocation invocation) {
        return new DatabaseActions(new org.neo4j.server.database.Database(invocation.getDatabase().getGraphDB()), leaseManager);
    }

    long extractId(Object uri) throws BadInputException {
        try {
            return Long.parseLong(uri.toString().substring(uri.toString().lastIndexOf("/") + 1));
        } catch (NumberFormatException ex) {
            throw new BadInputException(ex);
        } catch (NullPointerException ex) {
            throw new BadInputException(ex);
        }
    }

    protected OutputFormat createOutputFormat(Invocation invocation) throws URISyntaxException {
        final URI uri = new URI("/db/data"); // TODO invocation.getPath() is full path of route, need invocation.getBaseUri() from router
        return repository.outputFormat(Arrays.asList(MediaType.APPLICATION_JSON_TYPE), uri);
    }

    protected Long getNodeId(Invocation invocation) {
        return getLongParameter(invocation, NODE_ID_NAME);
    }
    protected Long getRelationshipId(Invocation invocation) {
        return getLongParameter(invocation, RELATIONSHIP_ID_NAME);
    }

    protected Long getLongParameter(Invocation invocation, String key) {
        return invocation.getPathVariables().getParamAsLong(key);
    }
    protected Long getLongParameter(Invocation invocation, String key, Long defaultValue) {
        final Long value = getLongParameter(invocation, key);
        return value == null ? defaultValue : value;
    }

    protected String getParameter(Invocation invocation, String key) {
        return invocation.getPathVariables().getParam(key);
    }

    protected Long getId(Invocation invocation) {
        return getLongParameter(invocation, "id");
    }


    protected String getKey(Invocation invocation) {
        return getParameter(invocation, "key");
    }

    protected Map readMap(Invocation invocation) {
        return invocation.getDeserializedContent(Map.class);
    }

    @SuppressWarnings("serial")
    public static class AmpersandSeparatedCollection extends LinkedHashSet<String> {
        public AmpersandSeparatedCollection(String path) {
            for (String e : path.split("&")) {
                if (e.trim()
                        .length() > 0) {
                    add(e);
                }
            }
        }
    }
}
