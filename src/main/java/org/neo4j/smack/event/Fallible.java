package org.neo4j.smack.event;

public interface Fallible {

    public void setFailed();

    public boolean hasFailed();
    
}
