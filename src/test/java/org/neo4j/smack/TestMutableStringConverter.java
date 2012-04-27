package org.neo4j.smack;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class TestMutableStringConverter {

    @Test
    public void testConvertToLongFromPositiveDecimalString() {
        assertThat(MutableStringConverter.toLongValue(new MutableString("0")), is(0l));
        assertThat(MutableStringConverter.toLongValue(new MutableString("10")), is(10l));
        assertThat(MutableStringConverter.toLongValue(new MutableString("1234567890")), is(1234567890l));
    }
    
    @Test(expected=NumberFormatException.class)
    public void testConvertingIllegalValueThrowsNumberFormatException() {
        assertThat(MutableStringConverter.toLongValue(new MutableString("0aabb")), is(0l));
    }
    
}
