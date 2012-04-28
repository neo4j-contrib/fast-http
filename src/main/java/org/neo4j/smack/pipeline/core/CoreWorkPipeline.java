package org.neo4j.smack.pipeline.core;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.neo4j.smack.pipeline.RingBufferWorkPipeline;
import org.neo4j.smack.pipeline.core.event.CorePipelineEvent;
import org.neo4j.smack.routing.InvocationVerb;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;

public class CoreWorkPipeline extends RingBufferWorkPipeline<CorePipelineEvent> implements WorkPublisher {

    public CoreWorkPipeline(ExceptionHandler exceptionHandler)
    {
        super("RequestEventHandler", CorePipelineEvent.FACTORY, exceptionHandler);
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

    public void addHandler(WorkHandler<CorePipelineEvent> handler)
    {
        super.addHandler(handler);
    }

}
