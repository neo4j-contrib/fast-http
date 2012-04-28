package org.neo4j.smack.pipeline.http;

import org.neo4j.smack.gcfree.MutableString;

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
