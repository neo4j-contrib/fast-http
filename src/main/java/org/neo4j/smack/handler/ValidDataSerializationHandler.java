package org.neo4j.smack.handler;

import org.neo4j.smack.event.ResponseEvent;

/**
 * @author mh
 * @since 11.12.11
 */
public class ValidDataSerializationHandler extends SerializationHandler {
    @Override
    public void onEvent(ResponseEvent event) throws Exception {
        if (event.hasFailed()) return;
        super.onEvent(event);
    }
}
