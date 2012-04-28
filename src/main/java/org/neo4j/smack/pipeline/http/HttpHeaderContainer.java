package org.neo4j.smack.pipeline.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.smack.gcfree.MutableString;

public class HttpHeaderContainer {

    /** 
     * Garbage free container type for header values.
     * Is not iterable (since that would mean we can't be 
     * garbage free), so instead to loop over, use a pattern like:
     * 
     * MutableString current = values.get(0);
     * for( int i=0, l=values.size() ; i<l ; current=values.get(++i)) {
     * 
     * }
     */
    class HttpHeaderValues {
        
        private MutableString [] values = new MutableString [2];
        private int numValues = 0;

        public HttpHeaderValues() {
            initializeValueStore(0, values.length);
        }
        
        public MutableString first()
        {
            if(numValues > 0) {
                return values[0];
            }
            return null;
        }

        /**
         * Copies the contents of value into a free slot.
         * @param value
         */
        public void add(MutableString value)
        {
            if(numValues == values.length) {
                makeInternalValueStoreBigger();
            }
            values[numValues++].setTo(value);
        }

        public MutableString get(int i)
        {
            if(i < numValues) {
                return values[i];
            }
            return null;
        }
        
        public int size() {
            return numValues;
        }

        public void clear()
        {
            numValues = 0;
        }
        
        protected int currentCapacity() {
            return values.length;
        }

        private void makeInternalValueStoreBigger()
        {
            int newStartIndex = values.length;
            values = Arrays.copyOf(values, values.length * 2);
            initializeValueStore(newStartIndex, values.length);
        }

        private void initializeValueStore(int newStartIndex, int length)
        {
            for(int i=newStartIndex;i<length;i++) {
                values[i] = new MutableString(16);
            }
        }
        
    }
    
    private Map<HttpHeaderName, HttpHeaderValues> headers = new HashMap<HttpHeaderName, HttpHeaderValues>();
    
    public MutableString getHeader(HttpHeaderName name)
    {
        if(headers.containsKey(name)) {
            return headers.get(name).first();
        } else {
            return null;
        }
    }

    public HttpHeaderValues getHeaders(HttpHeaderName name)
    {
        return headers.get(name);
    }

    public void addHeader(HttpHeaderName headerName, MutableString value)
    {
        if(!headers.containsKey(headerName)) {
            headers.put(headerName, new HttpHeaderValues());
        }
        headers.get(headerName).add(value);
    }

    public void clear()
    {
        // TODO: This creates an iterator, refactor.
        for(HttpHeaderValues v : headers.values()) {
            v.clear();
        }
    }

    public void removeHeader(HttpHeaderName name)
    {
        headers.get(name).clear();
    }

}
