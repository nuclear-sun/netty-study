package org.sun.herostory;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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
import org.sun.herostory.handler.CmdHandlerFactory;

/**
 * 服务端地址： http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=127.0.0.1:12345&userId=1
 */
public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {

        PropertyConfigurator.configure(ServerMain.class.getClassLoader().getResourceAsStream("log4j.properties"));

        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(65535),
                                new WebSocketServerProtocolHandler("/websocket"),
                                new GameMsgDecoder(),
                                new GameMsgEncoder(),
                                new GameMsgHandler()
                        );
                    }
                });
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture bind = serverBootstrap.bind(12345);

        try {

            ChannelFuture future = bind.sync();
            if(future.isSuccess()) {
                logger.info("服务已启动，端口： 12345");
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}
