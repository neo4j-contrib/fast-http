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
package org.neo4j.server.smack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public abstract class RoutingDefinition {
    
    protected LinkedHashMap<String, Object> routeDefs = new LinkedHashMap<String, Object>();
    
    public abstract void setupRoutes();
    
    public void addRoute(String route, Endpoint target) {
        routeDefs.put(route, target);
    }
    
    public void addRoute(String route, RoutingDefinition target) {
        routeDefs.put(route, target);
    }
        
    public void addRoute(String route, Object target) {
        routeDefs.put(route, new AnnotationBasedRoutingDefinition(target));
    }
    
    /**
     * Traverses the tree of routes of this RoutingDefinition,
     * and returns a list of route -> pipeline objects, with all
     * nested RoutingDefinitions resolved into their respective routes.
     */
    public List<RouteDefinitionEntry> getFlattenedRouteDefinition() {
        List<RouteDefinitionEntry> compoundRoutes = new ArrayList<RouteDefinitionEntry>();
        
        for(String route : routeDefs.keySet()) {
            
            Object target = routeDefs.get(route);
            
            if(target instanceof RoutingDefinition) {
                
                RoutingDefinition subRouter = (RoutingDefinition)target;
                
                subRouter.clearRoutes();
                subRouter.setupRoutes();
                
                List<RouteDefinitionEntry> subRoutes = subRouter.getFlattenedRouteDefinition();
                for(RouteDefinitionEntry subRoute : subRoutes) {
                    compoundRoutes.add(new RouteDefinitionEntry(route + subRoute.getPath(), subRoute.getEndpoint()));
                }
                
            } else {
                compoundRoutes.add(new RouteDefinitionEntry(route, (Endpoint)target));
            }
        }
        
        return compoundRoutes;
    }
    
    protected void clearRoutes() {
        routeDefs = new LinkedHashMap<String, Object>();
    }
}
