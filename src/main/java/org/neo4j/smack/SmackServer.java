/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.smack;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.server.smack.core.CreateErrorResponseHandler;
import org.neo4j.server.smack.core.PublishingExceptionHandler;
import org.neo4j.smack.event.RequestEvent;
import org.neo4j.smack.event.ResponseEvent;
import org.neo4j.smack.handler.*;
import org.neo4j.smack.routing.Endpoint;
import org.neo4j.smack.routing.Router;
import org.neo4j.smack.routing.RoutingDefinition;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class SmackServer {

    private int port;
    private String host;
    private Router router = new Router();
    private ServerBootstrap netty;
    private PipelineBootstrap<ResponseEvent> outputPipeline;
    private PipelineBootstrap<ResponseEvent> errorPipeline;
    private PipelineBootstrap<RequestEvent> inputPipeline;
    private ServerSocketChannelFactory channelFactory;
    private ChannelGroup openChannels = new DefaultChannelGroup("SmackServer");
    private Database database;
    private ExecutionHandler executionHandler;

    public SmackServer(String host, int port, Database db) {
        this.host = host;
        this.port = port;
        this.database = db;
    }

    public static void main(String[] args) throws IOException {
        final SmackServer smackServer = new SmackServer(args[0], Integer.parseInt(args[1]), new EmbeddedGraphDatabase(args[2]));
        smackServer.start();
        System.in.read();
        smackServer.stop();
    }

    public SmackServer(String host, int port, AbstractGraphDatabase graphDatabaseService) {
        this(host,port,new Database(graphDatabaseService));
    }
    
    @SuppressWarnings("unchecked")
    public void start() {
        
        router.compileRoutes();
        
        // OUTPUT PIPELINE

        errorPipeline = new PipelineBootstrap<ResponseEvent>(ResponseEvent.FACTORY, new WorkExceptionHandler(), new CreateErrorResponseHandler(), new SerializationHandler(), new WriteResponseHandler());
        errorPipeline.start();

        final PublishingExceptionHandler exceptionHandler = new PublishingExceptionHandler(errorPipeline.getRingBuffer());

        outputPipeline = new PipelineBootstrap<ResponseEvent>(ResponseEvent.FACTORY, exceptionHandler, new CreateResponseHandler(), new ValidDataSerializationHandler(), new WriteResponseHandler());
        outputPipeline.start();

        executionHandler = new ExecutionHandler(database, outputPipeline.getRingBuffer(), exceptionHandler);
        // INPUT PIPELINE

        inputPipeline = new PipelineBootstrap<RequestEvent>(RequestEvent.FACTORY, exceptionHandler, new RoutingHandler(router), new DeserializationHandler(), executionHandler);
        inputPipeline.start();

        // NETTY 
        
        channelFactory = 
            new OioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(new DaemonThreadFactory()),
                    Executors.newCachedThreadPool(new DaemonThreadFactory()));
        netty = new ServerBootstrap(channelFactory);

        // Set up the event pipeline factory.
        netty.setPipelineFactory(new NettyHttpPipelineFactory(inputPipeline.getRingBuffer(), openChannels));

        // Bind and start to accept incoming connections.
        openChannels.add(netty.bind(new InetSocketAddress(host, port)));

    }
    
    public void stop() {
        if (openChannels!=null) openChannels.close().awaitUninterruptibly();
        if (channelFactory!=null) channelFactory.releaseExternalResources();
        if (executionHandler!=null) executionHandler.stop();
        if (inputPipeline!=null) inputPipeline.stop();
        if (outputPipeline!=null) outputPipeline.stop();
        if (errorPipeline!=null) errorPipeline.stop();
    }
    
    public void addRoute(String route, RoutingDefinition target) {
        router.addRoute(route, target);
    }
    
    public void addRoute(String route, Endpoint target) {
        router.addRoute(route, target);
    }
    
    public void addRoute(String route, Object target) {
        router.addRoute(route, target);
    }
    
    public Database getDatabase() {
        return database;
    }
    
}
