package org.neo4j.smack.serialization.strategy;

import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.Serializer;

/**
 * Serializes a database property value.
 */
public class PropertyValueSerializationStrategy implements
        SerializationStrategy<Object> {

    @Override
    public void serialize(Object value, Serializer out)
    {
        if(value instanceof String) 
        {
            out.putString((String)value);
        } else if( value instanceof Integer) 
        {
            out.putInteger((Integer)value);
        } else if( value instanceof Boolean) 
        {
            out.putBoolean((Boolean)value);
        } else if( value instanceof Double) 
        {
            out.putDouble((Double)value);
        } else if( value instanceof Float) 
        {
            out.putFloat((Float)value);
        } else if( value instanceof Long) 
        {
            out.putLong((Long)value);
        } else if(value.getClass().isArray()) 
        {
            Class<?> cls = value.getClass();
            
            out.startList();
            
            if(cls.equals(String[].class))
            {
                for(String str : (String[])value) 
                {
                    out.putString(str);
                }
            } else if(cls.equals(int[].class)) 
            {
                for(int integer : (int[])value) 
                {
                    out.putInteger(integer);
                }
            } else if(cls.equals(boolean[].class)) 
            {
                for(boolean item : (boolean[])value) 
                {
                    out.putBoolean(item);
                }
            } else if(cls.equals(double[].class)) 
            {
                for(double item : (double[])value) 
                {
                    out.putDouble(item);
                }
            } else if(cls.equals(float[].class)) 
            {
                for(float item : (float[])value) 
                {
                    out.putFloat(item);
                }
            } else if(cls.equals(long[].class)) 
            {
                for(long item : (long[])value) 
                {
                    out.putLong(item);
                }
            } else 
            {
                throw new SerializationException("Unknown property value array type: " + value.getClass());
            }
            
            out.endList();
        } else 
        {
            throw new SerializationException("Unknown property value type: " + value.getClass());
        }
    }
    
    public void serialize(String value, Serializer out)
            throws SerializationException
    {
        out.putString(value);
    }
    
    public void serialize(int value, Serializer out)
            throws SerializationException
    {
        out.putInteger(value);
    }
    
    public void serialize(int [] value, Serializer out)
            throws SerializationException
    {
        out.startList();
        for(int i : value) {
            out.putInteger(i);
        }
        out.endList();
    }

}
