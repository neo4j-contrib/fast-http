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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.smack.handler.DatabaseWorkDivider;
import org.neo4j.smack.handler.DefaultExceptionHandler;
import org.neo4j.smack.handler.RoutingHandler;
import org.neo4j.smack.http.NettyHttpPipelineFactory;
import org.neo4j.smack.routing.Endpoint;
import org.neo4j.smack.routing.Router;
import org.neo4j.smack.routing.RoutingDefinition;

// TODO: Smell. Divide into multiple classes
public class SmackServer {

    private int port;
    private String host;
    private Router router = new Router();
    private ServerBootstrap netty;
    
    private InputPipeline inputPipeline;
    
    private ServerSocketChannelFactory channelFactory;
    private ChannelGroup openChannels = new DefaultChannelGroup("SmackServer");
    private Database database;
    private DatabaseWorkDivider executionHandler;
    private DefaultExceptionHandler exceptionHandler;

    public static void main(String[] args) throws IOException {
        final SmackServer smackServer = new SmackServer(args[0], Integer.parseInt(args[1]), new EmbeddedGraphDatabase(args[2]));
        smackServer.start();
        System.in.read();
        smackServer.stop();
    }

    public SmackServer(String host, int port, AbstractGraphDatabase graphDatabaseService) {
        this(host,port,new Database(graphDatabaseService));
    }
    
    public SmackServer(String host, int port, Database db) {
        this.host = host;
        this.port = port;
        this.database = db;
    }
    
    public void start() {
        
        router.compileRoutes();
        
        // MAIN PIPELINE

        exceptionHandler = new DefaultExceptionHandler();
        executionHandler = new DatabaseWorkDivider(database, exceptionHandler);

        inputPipeline = new InputPipeline(exceptionHandler, new RoutingHandler(router), executionHandler);
        inputPipeline.start();

        // NETTY 
        
        channelFactory = 
            new OioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(new DaemonThreadFactory("SocketMaster")),
                    Executors.newCachedThreadPool(new DaemonThreadFactory("SocketSlave")));
        netty = new ServerBootstrap(channelFactory);

        // Set up the event pipeline factory.
        netty.setPipelineFactory(new NettyHttpPipelineFactory(inputPipeline, openChannels));

        // Bind and start to accept incoming connections.
        openChannels.add(netty.bind(new InetSocketAddress(host, port)));

    }
    
    public void stop() {
        if (openChannels!=null) openChannels.close().awaitUninterruptibly();
        if (channelFactory!=null) channelFactory.releaseExternalResources();
        if (executionHandler!=null) executionHandler.stop();
        if (inputPipeline!=null) inputPipeline.stop();
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
