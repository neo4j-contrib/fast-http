package org.neo4j.server.smack.core;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.server.rest.domain.EndNodeNotFoundException;
import org.neo4j.server.rest.domain.StartNodeNotFoundException;
import org.neo4j.server.rest.repr.BadInputException;
import org.neo4j.server.rest.web.*;
import org.neo4j.server.smack.serialization.ExceptionSerializationStrategy;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.event.ResponseEvent;
import org.neo4j.smack.event.Result;
import org.neo4j.smack.routing.ResourceNotFoundException;
import org.neo4j.smack.serialization.SerializationStrategy;

import javax.management.relation.RelationNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mh
 * @since 05.12.11
 */
public class PublishingExceptionHandler implements ExceptionHandler {
    private final RingBuffer<ResponseEvent> errors;
    private final SerializationStrategy serializationStrategy;
    private Map<Class<? extends Throwable>, HttpResponseStatus> exceptionToStatusMap = new HashMap<Class<? extends Throwable>, HttpResponseStatus>() {{
        put(ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND);
        put(NodeNotFoundException.class, HttpResponseStatus.NOT_FOUND);
        put(RelationNotFoundException.class, HttpResponseStatus.NOT_FOUND);
        put(ArrayStoreException.class, HttpResponseStatus.BAD_REQUEST);
        put(BadInputException.class, HttpResponseStatus.BAD_REQUEST);
        put(OperationFailureException.class, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        put(NoSuchPropertyException.class, HttpResponseStatus.NOT_FOUND);
        put(RelationshipNotFoundException.class, HttpResponseStatus.NOT_FOUND);
        put(ClassCastException.class, HttpResponseStatus.BAD_REQUEST);
        put(StartNodeNotFoundException.class, HttpResponseStatus.NOT_FOUND);
        put(EndNodeNotFoundException.class, HttpResponseStatus.NOT_FOUND);
        put(PropertyValueException.class, HttpResponseStatus.BAD_REQUEST);
        put(UnsupportedOperationException.class, HttpResponseStatus.METHOD_NOT_ALLOWED);
        put(NotFoundException.class, HttpResponseStatus.NOT_FOUND);
    }};

    private HttpResponseStatus getErrorStatus(Exception ex) {
        for (Map.Entry<Class<? extends Throwable>, HttpResponseStatus> entry : exceptionToStatusMap.entrySet()) {
            if (entry.getKey().isInstance(ex)) return entry.getValue();
        }
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public PublishingExceptionHandler(RingBuffer<ResponseEvent> ringBuffer) {
        this.errors = ringBuffer;
        serializationStrategy = new ExceptionSerializationStrategy();
    }

    public void handle(Exception e, long sequence, Object event) {
        System.err.println("Error occurred while processing sequence " + sequence + " event " + event);
        e.printStackTrace();
        if (event instanceof RequestEvent) {
            System.err.println("[S] Failing request was: " + ((RequestEvent)event).getId());
            handleRequest((RequestEvent) event, e);
            return;
        }
        if (event instanceof ResponseEvent) {
            System.err.println("[S] Failing response was: " + ((ResponseEvent)event).getId());
            handleResponse((ResponseEvent) event, e);
        }
    }

    private void handleResponse(ResponseEvent event, Exception e) {
        event.setFailed();
        publishError(extractResult(event), e);
    }

    private Result extractResult(ResponseEvent event) {
        final Result result = event.getInvocationResult();
        result.setContext(event.getContext());
        return result;
    }

    private void handleRequest(RequestEvent event, Exception e) {
        event.setFailed();
        publishError(extractResult(event), e);
    }

    private Result extractResult(RequestEvent event) {
        final Result result = new Result();
        result.setContext(event.getContext());
        return result;
    }

    private void publishError(Result result, Exception e) {
        result.setData(e);
        result.setStatus(getErrorStatus(e));

        long sequenceId = errors.next();
        ResponseEvent ev = errors.get(sequenceId);
        ev.setSerializationStrategy(serializationStrategy); // todo
        ev.setContext(result.getContext());
        ev.setInvocationResult(result);
        ev.setFailed();
        errors.publish(sequenceId);
    }
}
