package org.neo4j.smack.test.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonHelper
{

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings( "unchecked" )
    public static Map<String, Object> jsonToMap( String json ) throws JsonParseException, IOException
    {
        return (Map<String, Object>) readJson( json );
    }

    @SuppressWarnings( "unchecked" )
    public static List<Map<String, Object>> jsonToList( String json ) throws JsonParseException, IOException
    {
        return (List<Map<String, Object>>) readJson( json );
    }
    
    public static Object readJson( String json ) throws JsonParseException, IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue( json, Object.class );
    }

    public static Object jsonToSingleValue( String json ) throws JsonParseException, IOException 
    {
        Object jsonObject = readJson( json );
        return jsonObject instanceof Collection<?> ? jsonObject : assertSupportedPropertyValue( jsonObject );
    }

    private static Object assertSupportedPropertyValue( Object jsonObject ) 
    {
        if ( jsonObject == null )
        {
            throw new RuntimeException( "null value not supported" );

        }

        if ( jsonObject instanceof String )
        {
        }
        else if ( jsonObject instanceof Number )
        {
        }
        else if ( jsonObject instanceof Boolean )
        {
        }
        else
        {
            throw new RuntimeException(
                    "Unsupported value type " + jsonObject.getClass() + "."
                            + " Supported value types are all java primitives (byte, char, short, int, "
                            + "long, float, double) and String, as well as arrays of all those types" );
        }
        return jsonObject;
    }

    public static String createJsonFrom( Object data ) throws IOException 
    {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = OBJECT_MAPPER.getJsonFactory()
                .createJsonGenerator( writer )
                .useDefaultPrettyPrinter();
        OBJECT_MAPPER.writeValue( generator, data );
        writer.close();
        return writer.getBuffer()
                .toString();
    }
}
