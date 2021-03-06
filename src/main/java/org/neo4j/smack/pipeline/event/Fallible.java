package org.neo4j.smack.pipeline.event;

public interface Fallible {

    public void setFailed(Throwable exception);
    
    public Throwable getFailureCause();

    public boolean hasFailed();
    
}
