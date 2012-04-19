package org.neo4j.smack.routing;


public interface Routable {

    String getPath();

    InvocationVerb getVerb();

    PathVariables getPathVariables();

}
