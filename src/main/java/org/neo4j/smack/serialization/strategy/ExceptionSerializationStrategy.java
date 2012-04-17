package org.neo4j.smack.serialization.strategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.smack.serialization.AbstractNonStreamingSerializationStrategy;
import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.Serializer;

/**
 * @author mh
 * @since 27.11.11
 */
public class ExceptionSerializationStrategy extends AbstractNonStreamingSerializationStrategy<Throwable> {

    @Override
    public void serialize(Throwable exception, Serializer out) throws SerializationException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        addMessage(exception, result);
        addException(exception, result);
        addStackTrace(exception, result);
        out.putMap(result);
    }

    private void addException(Throwable exception, Map<String, Object> result) {
        result.put("exception", exception.toString());
    }

    private void addMessage(Throwable exception, Map<String, Object> result) {
        String message = exception.getMessage();
        if (message == null) return;
        result.put("message", message);
    }

    private void addStackTrace(Throwable exception, Map<String, Object> result) {
        StackTraceElement[] trace = exception.getStackTrace();
        if (trace == null) return;

        List<String> list = new ArrayList<String>(trace.length);
        for (StackTraceElement traceElement : trace) {
            list.add(traceElement.toString());
        }
        result.put("stacktrace", list);
    }
}
