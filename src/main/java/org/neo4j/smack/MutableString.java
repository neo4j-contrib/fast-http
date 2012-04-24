package org.neo4j.smack;

import java.util.Arrays;

/**
 * A mutable String implementation. Uses more memory than
 * native java strings and are slower, but can be reused.
 * 
 * In high-throughput applications, this can be very useful
 * as the performance impact of GC is high, and we can pool
 * mutable string objects, so we don't need GC.
 */
public class MutableString {
    
    private char [] chars;
    private int length = 0;

    public MutableString(String string)
    {
        chars = string.toCharArray();
        length = chars.length;
    }

    public MutableString(int initialCapacity)
    {
        chars = new char[initialCapacity];
        length = 0;
    }

    public void append(char character)
    {
        try {
            chars[length++] = character;
        } catch(ArrayIndexOutOfBoundsException e) {
            ensureCapacity(length);
            chars[length] = character;
        }
    }

    public void setTo(MutableString value)
    {
        ensureCapacity(value.length);
        length = value.length;
        char [] other = value.chars;
        for(int i=0;i<length;i++) {
            chars[i] = other[i];
        }
    }

    public void reset()
    {
        length = 0;
    }
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof MutableString) {
            return equals((MutableString)other);
        }
        return false;
    }
    
    public boolean equals(MutableString other) {
        if(other != null && other.length == length) {
            char[] otherChars = other.chars;
            for(int i=0;i<length;i++) {
                if(otherChars[i] != chars[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean equalsIgnoreCase(MutableString other)
    {
        if(other != null && other.length == length) {
            char[] otherChars = other.chars;
            for(int i=0;i<length;i++) {
                if(Character.toLowerCase(otherChars[i]) != Character.toLowerCase(chars[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "MutableString['"+ new String(Arrays.copyOf(chars, length)) + "']";
    }
    
    private void ensureCapacity(int capacity) {
        if(chars.length <= capacity) {
            int newSize = chars.length;
            do {
                newSize = newSize * 2;
            } while(newSize < capacity);
            
            // Resize
            chars = Arrays.copyOf(chars, newSize);
        }
    }
}
