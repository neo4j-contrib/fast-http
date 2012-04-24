package org.neo4j.smack;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.neo4j.smack.routing.InvocationVerb;

public interface WorkPublisher {

    void addWork(Long connectionId, InvocationVerb verb, String path,
            ChannelBuffer content, Channel channel, boolean keepAlive);

    void addFailure(Long connectionId, Channel channel, Throwable cause);

}
