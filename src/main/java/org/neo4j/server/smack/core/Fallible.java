package org.neo4j.server.smack.core;

public interface Fallible {

    public void setFailure(Throwable e);
    
    public Throwable getFailure();
    
    public boolean hasFailed();
    
}
