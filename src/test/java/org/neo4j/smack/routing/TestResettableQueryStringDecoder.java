package org.neo4j.smack.routing;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jboss.netty.util.CharsetUtil;
import org.junit.Test;


public class TestResettableQueryStringDecoder {

    @Test
    public void testDecodeAndReset() {
        ResettableQueryStringDecoder decoder = new ResettableQueryStringDecoder();
        decoder.reset("/db/data", CharsetUtil.UTF_8);
        
        assertThat(decoder.getParameters().size(), is(0)); 
        
        decoder.reset("/db/da", CharsetUtil.UTF_8);
        
        assertThat(decoder.getParameters().size(), is(0)); 
    }
     
}
