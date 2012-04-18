package org.neo4j.smack.event;


public interface Output {
    
    public void created();

    public void created(Object value);

    public void createdAt(String location);
    
    public void createdAt(String location, Object value);

    public void ok();

    public void ok(Object value);

    public void okNoContent();

    public void okAt(String location, Object value);

    public void notFound();
    
}
