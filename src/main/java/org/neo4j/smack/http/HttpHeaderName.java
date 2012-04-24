package org.neo4j.smack.http;

import org.neo4j.smack.MutableString;

public class HttpHeaderName {

    private MutableString name;
    
    public HttpHeaderName(String string)
    {
        this.name = new MutableString(string);
    }

    public boolean equalsString(MutableString headerName)
    {
        return headerName.equals(name);
    }
    
}
