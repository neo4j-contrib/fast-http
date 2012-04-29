package org.neo4j.smack.pipeline.core;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.neo4j.smack.pipeline.CombinedHandler;
import org.neo4j.smack.pipeline.RingBufferWorkPipeline;
import org.neo4j.smack.pipeline.core.event.CorePipelineEvent;
import org.neo4j.smack.routing.InvocationVerb;

import com.lmax.disruptor.ExceptionHandler;

public class CoreWorkPipeline extends RingBufferWorkPipeline<CorePipelineEvent> implements WorkPublisher {

    @SuppressWarnings("unchecked")
    public CoreWorkPipeline(ExceptionHandler exceptionHandler,
            RoutingHandler routingHandler, DeserializationHandler deserializationHandler,
            TransactionPreparationHandler tranasctionPreparationHandler, WorkDivisionHandler workDivisionHandler)
    {
        super("CoreWorkEventHandler", CorePipelineEvent.FACTORY, exceptionHandler, 1024 * 4);
        addHandler(new CombinedHandler<CorePipelineEvent>(
                routingHandler, 
                deserializationHandler));
        addHandler(new CombinedHandler<CorePipelineEvent>(
                tranasctionPreparationHandler, 
                workDivisionHandler));
    }

    @Override
    public void addWork(Long connectionId, InvocationVerb verb, String path,
            ChannelBuffer content, Channel channel, boolean keepAlive)
    {
        long sequenceNo = ringBuffer.next();
        CorePipelineEvent event = ringBuffer.get(sequenceNo);

        event.reset(connectionId, verb, path, content, channel, keepAlive);

        ringBuffer.publish(sequenceNo);
    }

    @Override
    public void addFailure(Long connectionId, Channel channel, Throwable cause)
    {
        long sequenceNo = ringBuffer.next();
        CorePipelineEvent event = ringBuffer.get(sequenceNo);

        event.reset(connectionId, channel, cause);

        ringBuffer.publish(sequenceNo);
    }

}
