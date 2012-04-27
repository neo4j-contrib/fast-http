package org.neo4j.smack;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.routing.InvocationVerb;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;

public class InputPipeline extends PipelineBootstrap<RequestEvent> implements WorkPublisher {

    public InputPipeline(ExceptionHandler exceptionHandler)
    {
        super("RequestEventHandler", RequestEvent.FACTORY, exceptionHandler);
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

    @Override
    public void addFailure(Long connectionId, Channel channel, Throwable cause)
    {
        long sequenceNo = ringBuffer.next();
        RequestEvent event = ringBuffer.get(sequenceNo);

        event.reset(connectionId, channel, cause);

        ringBuffer.publish(sequenceNo);
    }

    public void addHandler(WorkHandler<RequestEvent> handler)
    {
        super.addHandler(handler);
    }

}
