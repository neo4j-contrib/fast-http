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

import com.sun.jersey.server.impl.uri.PathPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

public class PathVariables {
    
    private Map<String,String> pathVariables = new HashMap<String, String>();
    
    public void add(MatchResult matched, PathPattern routePattern) {
        List<String> vars = routePattern.getTemplate().getTemplateVariables();
        for(int i=0,l=vars.size();i<l;i++) {
            pathVariables.put(vars.get(i), matched.group(i+1));
        }
    }

    public Long getParamAsLong(String key) {
        if (getParam(key)==null) {
            return null;
        }
        return Long.valueOf(getParam(key));
    }

    public String getParam(String key) {
        return pathVariables.get(key);
    }

    // todo only handle first parameter so far
    public void add(Map<String, List<String>> parameters) {
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (entry.getValue()==null && entry.getValue().isEmpty()) continue;
            pathVariables.put(entry.getKey(),entry.getValue().get(0));
        }
    }
}
