package org.neo4j.smack.api;


/**
 * @author mh
 * @since 05.12.11
 */
public class RelationshipService extends RestService {
//    private static final String PATH_NODE_RELATIONSHIPS_W_DIR = PATH_NODE_RELATIONSHIPS
//            + "/{direction}";
//    private static final String PATH_NODE_RELATIONSHIPS_W_DIR_N_TYPES = PATH_NODE_RELATIONSHIPS_W_DIR
//            + "/{types}";
//    private static final String PATH_RELATIONSHIP_PROPERTIES = PATH_RELATIONSHIP
//            + "/properties";
//    private static final String PATH_RELATIONSHIP_PROPERTY = PATH_RELATIONSHIP_PROPERTIES
//            + "/{key}";
//
    public RelationshipService(String dataPath)
    {
        super(dataPath);
    }
//
//    @POST
//    @Path(PATH_NODE_RELATIONSHIPS)
//    @SuppressWarnings("unchecked")
//    @DeserializeWith(PropertyMapDeserializationStrategy.class)
//    //@SerializeWith(RepresentationSerializationStrategy.class)
//    public void createRelationship(Invocation invocation, Output result)
//            throws BadInputException, URISyntaxException,
//            NodeNotFoundException, OperationFailureException,
//            NoSuchPropertyException, StartNodeNotFoundException,
//            EndNodeNotFoundException
//    {
//        final Map<String, Object> data = invocation.getContent();
//        final long endNodeId = extractId(data.get("to"));
//        final String type = (String) data.get("type");
//        final Map<String, Object> properties = (Map<String, Object>) data
//                .get("data");
//        final RelationshipRepresentation relationship = actionsFor(invocation)
//                .createRelationship(getNodeId(invocation), endNodeId, type,
//                        properties);
//        final String location = createOutputFormat(invocation).format(
//                relationship.selfUri());
//        result.createdAt(location, relationship);
//    }
//
//    @GET
//    @Path(PATH_RELATIONSHIP)
//    public void getRelationship(Invocation invocation, Output result)
//            throws Exception
//    {
//        result.ok(actionsFor(invocation).getRelationship(
//                getRelationshipId(invocation)));
//    }
//
//    @DELETE
//    @Path(PATH_RELATIONSHIP)
//    public void deleteRelationship(Invocation invocation, Output result)
//            throws Exception
//    {
//        actionsFor(invocation)
//                .deleteRelationship(getRelationshipId(invocation));
//        result.ok();
//    }
//
//    @GET
//    @Path(PATH_NODE_RELATIONSHIPS_W_DIR)
//    //@SerializeWith(RepresentationSerializationStrategy.class)
//    public void getNodeRelationships(Invocation invocation, Output result)
//            throws Exception
//    {
//        final DatabaseActions.RelationshipDirection direction = DatabaseActions.RelationshipDirection
//                .valueOf(getParameter(invocation, "direction"));
//        result.ok(actionsFor(invocation).getNodeRelationships(
//                getNodeId(invocation), direction,
//                Collections.<String> emptyList()));
//    }
//
//    @GET
//    @Path(PATH_NODE_RELATIONSHIPS_W_DIR_N_TYPES)
//    //@SerializeWith(RepresentationSerializationStrategy.class)
//    public void getNodeRelationshipsWithDirAndTypes(Invocation invocation,
//            Output result) throws Exception
//    {
//        final DatabaseActions.RelationshipDirection direction = DatabaseActions.RelationshipDirection
//                .valueOf(getParameter(invocation, "direction"));
//        final AmpersandSeparatedCollection types = new AmpersandSeparatedCollection(
//                getParameter(invocation, "types"));
//        result.ok(actionsFor(invocation).getNodeRelationships(
//                getNodeId(invocation), direction, types));
//    }
//
//    @GET
//    @Path(PATH_RELATIONSHIP_PROPERTIES)
//    //@SerializeWith(RepresentationSerializationStrategy.class)
//    public void getAllRelationshipProperties(Invocation invocation,
//            Output result) throws Exception
//    {
//        final PropertiesRepresentation properties = actionsFor(invocation)
//                .getAllRelationshipProperties(getRelationshipId(invocation));
//        if (!properties.isEmpty())
//        {
//            result.ok(properties);
//        } else
//        {
//            result.ok();
//        }
//    }
//
//    @GET
//    @Path(PATH_RELATIONSHIP_PROPERTY)
//    //@SerializeWith(RepresentationSerializationStrategy.class)
//    public void getRelationshipProperty(Invocation invocation, Output result)
//            throws Exception
//    {
//        result.ok(actionsFor(invocation).getRelationshipProperty(
//                getRelationshipId(invocation), getKey(invocation)));
//    }
//
//    @PUT
//    @Path(PATH_RELATIONSHIP_PROPERTIES)
//    public void setAllRelationshipProperties(Invocation invocation,
//            Output result) throws Exception
//    {
//        actionsFor(invocation).setAllRelationshipProperties(
//                getRelationshipId(invocation), readMap(invocation));
//        result.ok();
//    }
//
//    @PUT
//    @Path(PATH_RELATIONSHIP_PROPERTY)
//    @DeserializeWith(ValueDeserializationStrategy.class)
//    public void setRelationshipProperty(Invocation invocation, Output result)
//            throws Exception
//    {
//        actionsFor(invocation).setRelationshipProperty(
//                getRelationshipId(invocation), getKey(invocation),
//                invocation.getContent());
//        result.ok();
//    }
//
//    @DELETE
//    @Path(PATH_RELATIONSHIP_PROPERTIES)
//    public void deleteAllRelationshipProperties(Invocation invocation,
//            Output result) throws Exception
//    {
//        actionsFor(invocation).removeAllRelationshipProperties(
//                getRelationshipId(invocation));
//        result.ok();
//    }
//
//    @DELETE
//    @Path(PATH_RELATIONSHIP_PROPERTY)
//    public void deleteRelationshipProperty(Invocation invocation, Output result)
//            throws Exception
//    {
//        actionsFor(invocation).removeRelationshipProperty(
//                getRelationshipId(invocation), getKey(invocation));
//        result.ok();
//    }
}