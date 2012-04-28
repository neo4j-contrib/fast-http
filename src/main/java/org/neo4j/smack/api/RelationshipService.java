package org.neo4j.smack.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.smack.pipeline.database.event.Invocation;
import org.neo4j.smack.pipeline.database.event.Output;
import org.neo4j.smack.routing.annotation.DeserializeWith;
import org.neo4j.smack.routing.annotation.SerializeWith;
import org.neo4j.smack.routing.annotation.Transactional;
import org.neo4j.smack.serialization.strategy.PropertyContainerDeserialization;
import org.neo4j.smack.serialization.strategy.PropertyContainerDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.PropertyValueDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.RelationshipCreationDescription;
import org.neo4j.smack.serialization.strategy.RelationshipCreationDeserializationStrategy;
import org.neo4j.smack.serialization.strategy.RelationshipSerializationStrategy;

public class RelationshipService extends BasePropertyContainerService
{
    
    @POST
    @Transactional
    @Path(UrlReverseLookerUpper.PATH_NODE_RELATIONSHIPS)
    @DeserializeWith(RelationshipCreationDeserializationStrategy.class)
    @SerializeWith(RelationshipSerializationStrategy.class)
    public void createRelationship(Invocation invocation, Output result)
    {
        GraphDatabaseService db = invocation.getDB();
        RelationshipCreationDescription relToCreate = invocation.getContent();
        
        Node from = db.getNodeById(getNodeId(invocation));
        Node to   = db.getNodeById(relToCreate.getEndNodeId());
        
        Relationship rel = from.createRelationshipTo(to, relToCreate.getType());
        setProperties(rel, relToCreate);
        
        result.createdAt(url.reverse(rel), rel);
    }

    @GET
    @Path(UrlReverseLookerUpper.PATH_RELATIONSHIP)
    @SerializeWith(RelationshipSerializationStrategy.class)
    public void getRelationship(Invocation invocation, Output result)
            throws Exception
    {
        GraphDatabaseService db = invocation.getDB();
        result.ok(db.getRelationshipById(getRelationshipId(invocation)));
    }

    @DELETE
    @Transactional
    @Path(UrlReverseLookerUpper.PATH_RELATIONSHIP)
    public void deleteRelationship(Invocation invocation, Output result)
            throws Exception
    {
        invocation.getDB().getRelationshipById(getRelationshipId(invocation)).delete();
        result.ok();
    }

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

    @PUT
    @Transactional
    @Path(UrlReverseLookerUpper.PATH_RELATIONSHIP_PROPERTIES)
    @DeserializeWith(PropertyContainerDeserializationStrategy.class)
    public void setAllRelationshipProperties(Invocation invocation,
            Output result) throws Exception
    {
        Relationship rel = invocation.getDB().getRelationshipById(getRelationshipId(invocation));
        removeAllProperties(rel);
        setProperties(rel, invocation.<PropertyContainerDeserialization>getContent());
        result.okNoContent();
    }

    @PUT
    @Transactional
    @Path(UrlReverseLookerUpper.PATH_RELATIONSHIP_PROPERTY)
    @DeserializeWith(PropertyValueDeserializationStrategy.class)
    public void setRelationshipProperty(Invocation invocation, Output result)
            throws Exception
    {
        Relationship rel = invocation.getDB().getRelationshipById(getRelationshipId(invocation));
        rel.setProperty(getPropertyKey(invocation), invocation.getContent());
        result.okNoContent();
    }

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