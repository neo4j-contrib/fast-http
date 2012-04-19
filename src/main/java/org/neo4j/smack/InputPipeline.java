package org.neo4j.smack;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.handler.DatabaseWorkDivider;
import org.neo4j.smack.handler.RoutingHandler;
import org.neo4j.smack.routing.InvocationVerb;

import com.lmax.disruptor.ExceptionHandler;

public class InputPipeline extends PipelineBootstrap<RequestEvent> implements WorkInputGate {

    @SuppressWarnings("unchecked")
    public InputPipeline(ExceptionHandler exceptionHandler, RoutingHandler routingHandler, DatabaseWorkDivider workDivider)
    {
        super("RequestEventHandler", RequestEvent.FACTORY, exceptionHandler, routingHandler, workDivider);
    }

    @Override
    public void addWork(Long connectionId, InvocationVerb verb, String path,
            ChannelBuffer content, Channel channel, boolean keepAlive)
    {
        long sequenceNo = ringBuffer.next();
        RequestEvent event = ringBuffer.get(sequenceNo);

        event.reset(connectionId, verb, path, content, channel, keepAlive);

        ringBuffer.publish(sequenceNo);
    }

}
