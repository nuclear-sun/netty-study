package org.sun.herostory;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {

        PropertyConfigurator.configure(ServerMain.class.getClassLoader().getResourceAsStream("log4j.properties"));

        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);

        ServerBootstrap serverBootstrap = null;

        try {
            serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        protected void initChannel(NioSocketChannel ch) throws Exception {

                            ch.pipeline().addLast(
                                    new HttpServerCodec(),
                                    new HttpObjectAggregator(65535),
                                    new WebSocketServerProtocolHandler("/websocket"),
                                    new GameMsgHandler()
                            );
                        }
                    });
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.bind(9999);

        } catch (Exception e) {
            if(serverBootstrap != null) {

            }
        }


    }
}
