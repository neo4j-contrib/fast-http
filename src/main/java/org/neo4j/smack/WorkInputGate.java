package org.neo4j.smack;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.neo4j.smack.routing.InvocationVerb;

public interface WorkInputGate {

    void addWork(Long connectionId, InvocationVerb verb, String path,
            ChannelBuffer content, Channel channel, boolean keepAlive);

}
