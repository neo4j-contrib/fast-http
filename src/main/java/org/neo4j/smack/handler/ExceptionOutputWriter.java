package org.neo4j.smack.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.management.relation.RelationNotFoundException;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.server.rest.domain.EndNodeNotFoundException;
import org.neo4j.server.rest.domain.StartNodeNotFoundException;
import org.neo4j.server.rest.repr.BadInputException;
import org.neo4j.server.rest.web.NoSuchPropertyException;
import org.neo4j.server.rest.web.NodeNotFoundException;
import org.neo4j.server.rest.web.OperationFailureException;
import org.neo4j.server.rest.web.PropertyValueException;
import org.neo4j.server.rest.web.RelationshipNotFoundException;
import org.neo4j.smack.event.NettyChannelBackedOutput;
import org.neo4j.smack.routing.ResourceNotFoundException;
import org.neo4j.smack.serialization.SerializationStrategy;
import org.neo4j.smack.serialization.strategy.ExceptionSerializationStrategy;

/**
 * @author mh
 * @since 05.12.11
 */
public class ExceptionOutputWriter {
    
    private final SerializationStrategy<Throwable> exceptionSerializationStrategy;
    
    private Map<Class<? extends Throwable>, HttpResponseStatus> exceptionToStatusMap = new HashMap<Class<? extends Throwable>, HttpResponseStatus>() 
            {
        
        private static final long serialVersionUID = -5937199711856466595L;

    {
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

    public ExceptionOutputWriter() {
        exceptionSerializationStrategy = new ExceptionSerializationStrategy();
    }

    public void write(NettyChannelBackedOutput output, Exception e)
    {
        e.printStackTrace();
        output.send(getErrorStatus(e), e, null, exceptionSerializationStrategy);
    }

    private HttpResponseStatus getErrorStatus(Throwable ex) {
        if (ex instanceof InvocationTargetException) {
            ex = ((InvocationTargetException)ex).getTargetException();
        }
        for (Map.Entry<Class<? extends Throwable>, HttpResponseStatus> entry : exceptionToStatusMap.entrySet()) {
            if (entry.getKey().isInstance(ex)) return entry.getValue();
        }
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
