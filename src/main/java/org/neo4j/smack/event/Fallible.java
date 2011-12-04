package org.neo4j.smack.event;

public interface Fallible {

    public void setFailure(Throwable e);
    
    public Throwable getFailure();
    
    public boolean hasFailed();
    
}
