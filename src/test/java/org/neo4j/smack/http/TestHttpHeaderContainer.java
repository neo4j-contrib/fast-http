package org.neo4j.smack.http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.neo4j.smack.MutableString;


public class TestHttpHeaderContainer {

    @Test
    public void testAssignAndClear() {
        HttpHeaderContainer headers = new HttpHeaderContainer();
        
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        assertThat(headers.getHeaders(HttpHeaderNames.CONTENT_LENGTH).size(), is(1));
        
        headers.clear();
        
        assertThat(headers.getHeaders(HttpHeaderNames.CONTENT_LENGTH).size(), is(0));
    }
    
    @Test
    public void testInternalStorageGetsReused() {
        HttpHeaderContainer headers = new HttpHeaderContainer();
        
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        
        int preClearCapacity = headers.getHeaders(HttpHeaderNames.CONTENT_LENGTH).currentCapacity();
        
        headers.clear();

        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        headers.addHeader(HttpHeaderNames.CONTENT_LENGTH, new MutableString("0"));
        
        assertThat(headers.getHeaders(HttpHeaderNames.CONTENT_LENGTH).currentCapacity(), is(preClearCapacity));
    }
    
}
