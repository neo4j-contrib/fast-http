package org.neo4j.smack;

public class MutableStringConverter {

    /**
     * Garbage free number parser, reads a mutable string
     * and returns a long.
     * 
     * Currently only does positive decimal numbers, expand
     * as necessary.
     * 
     * @param value
     * @return
     */
    public static long toLongValue(MutableString value)
    {
        long longValue = 0,
             multiplier = 1,
             digit;
        char [] chars = value.getChars();
        
        for(int i=value.getLength()-1,l=0;i>=l;i--) {
            digit = Character.digit(chars[i], 10);
            
            if(digit != -1) {
                longValue = longValue + (digit * multiplier);
                multiplier *= 10;
            } else {
                throw new NumberFormatException("I don't know how to convert " + value + " to a long.");
            }
        }
        return longValue;
    }

}
