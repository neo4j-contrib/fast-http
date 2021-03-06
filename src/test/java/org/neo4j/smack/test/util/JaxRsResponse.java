package org.neo4j.smack.test.util;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.StringKeyObjectValueIgnoreCaseMultivaluedMap;

public class JaxRsResponse extends Response
{

    private final int status;
    private final MultivaluedMap<String,Object> metaData;
    private final MultivaluedMap<String, String> headers;
    private final URI location;
    private String data;
    private MediaType type;

    public JaxRsResponse( ClientResponse response )
    {
        this(response, extractContent(response));
    }

    private static String extractContent(ClientResponse response) {
        if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) return null;
        return response.getEntity(String.class);
    }

    public JaxRsResponse(ClientResponse response, String entity) {
        status = response.getStatus();
        metaData = extractMetaData(response);
        headers = extractHeaders(response);
        location = response.getLocation();
        type = response.getType();
        data = entity;
        response.close();
    }

    @Override
    public String getEntity()
    {
        return data;
    }

    @Override
    public int getStatus()
    {
        return status;
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata()
    {
        return metaData;
    }

    private MultivaluedMap<String, Object> extractMetaData(ClientResponse jettyResponse) {
        MultivaluedMap<String, Object> metadata = new StringKeyObjectValueIgnoreCaseMultivaluedMap();
        for ( Map.Entry<String, List<String>> header : jettyResponse.getHeaders()
                .entrySet() )
        {
            for ( Object value : header.getValue() )
            {
                metadata.putSingle( header.getKey(), value );
            }
        }
        return metadata;
    }

    public MultivaluedMap<String, String> getHeaders()
    {
        return headers;
    }

    private MultivaluedMap<String, String> extractHeaders(ClientResponse jettyResponse) {
        return jettyResponse.getHeaders();
    }

    // new URI( getHeaders().get( HttpHeaders.LOCATION ).get(0));
    public URI getLocation()
    {
        return location;
    }

    public void close()
    {

    }

    public static JaxRsResponse extractFrom(ClientResponse clientResponse) {
        return new JaxRsResponse(clientResponse);
    }

    public MediaType getType() {
        return type;
    }

    public String getEntity(Class<String> type) {
        return getEntity();
    }
}
