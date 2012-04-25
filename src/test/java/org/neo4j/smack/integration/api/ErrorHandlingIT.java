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
package org.neo4j.smack.integration.api;

import org.junit.Test;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.routing.Endpoint;
import org.neo4j.smack.test.util.AbstractRestFunctionalTestBase;
import org.neo4j.smack.test.util.JaxRsResponse;
import org.neo4j.smack.test.util.RestRequest;

import static org.junit.Assert.assertEquals;

public class ErrorHandlingIT extends AbstractRestFunctionalTestBase {

    @Test
    public void shouldReturn404OnMissingResource() throws Exception {
        JaxRsResponse response = RestRequest.req().post(getDataUri() + "some/bs/resource123/lol", "[]");
        assertEquals(404, response.getStatus());
        response.close();
    }
}
