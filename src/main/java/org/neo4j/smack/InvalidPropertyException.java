package org.neo4j.smack;

public class InvalidPropertyException extends RuntimeException {

    public InvalidPropertyException(String message)
    {
        super(message);
    }

    private static final long serialVersionUID = 1L;

}
