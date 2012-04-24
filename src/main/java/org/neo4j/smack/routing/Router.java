/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.smack.routing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.MatchResult;

import org.apache.log4j.Logger;

import com.sun.jersey.server.impl.uri.PathPattern;
import com.sun.jersey.server.impl.uri.PathTemplate;

public class Router extends RoutingDefinition {

    private RouteEntry [] routes;
    private static final Logger logger = Logger.getLogger(Router.class);
    
    private static Endpoint notFoundEndpoint = new NotFoundEndpoint();
    
    private final ResettableQueryStringDecoder queryStringDecoder = new ResettableQueryStringDecoder();
    
    public Endpoint route(Routable routable)
    {
        
        String path = routable.getPath();
        
        queryStringDecoder.resetWith(path);
        routable.getPathVariables().add(queryStringDecoder.getParameters());
        
        for(RouteEntry route : routes) // todo parallelize routing ?? (overhead ?)
        {
            MatchResult matchResult = route.pattern.match(path);
            if(matchResult != null)
            {
                Endpoint endpoint = route.getEndpoint(routable.getVerb());
                if(endpoint != null) {
                    routable.getPathVariables().add(matchResult, route.pattern); // todo is this the best way ?
                    return endpoint;
                }
                return notFoundEndpoint;
            }
        }
        return notFoundEndpoint;
    }
    
    public void compileRoutes() {
        Map<String, RouteEntry> routeMap = new LinkedHashMap<String, RouteEntry>();

        for(RouteDefinitionEntry definition : getRouteDefinitionEntries())
        {
            if(!routeMap.containsKey(definition.getPath()))
            {
                logger.debug("Adding Route: "+definition.getEndpoint().getVerb() +" to: "+ definition.getPath());
                routeMap.put(definition.getPath(), createRoute(definition));
            }

            RouteEntry route = routeMap.get(definition.getPath());
            route.setEndpoint(definition.getEndpoint().getVerb(), definition.getEndpoint());
            // todo what happens if multiple paths have differnt verbs?
        }
        
        for(RouteEntry route : routeMap.values()) 
            System.out.println(route);
        
        routes = routeMap.values().toArray(new RouteEntry[routeMap.size()]);
    }

    private RouteEntry createRoute(RouteDefinitionEntry definition) {
        RouteEntry route = new RouteEntry();
        final PathTemplate template = new PathTemplate(definition.getPath());
        route.pattern = new PathPattern(template, definition.getPath().equals("") ? "/" : "");
        return route;
    }
}
