package org.sun.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.handler.*;
import org.sun.herostory.model.User;
import org.sun.herostory.model.UserManager;
import org.sun.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    private Logger logger = LoggerFactory.getLogger(GameMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(ctx == null) {
            return;
        }
        try {
            super.channelActive(ctx);
            Broadcaster.addChannel(ctx.channel());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    // 重写 channelInactive 和 handlerRemoved 都可以做关闭清理

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(ctx == null) {
            return;
        }
        try {
            Broadcaster.removeChannel(ctx.channel());
            Object userId = ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            if(userId == null) {
                return;
            }

            UserManager.removeUserById((Integer)userId);

            GameMsgProtocol.UserQuitResult.Builder builder = GameMsgProtocol.UserQuitResult.newBuilder();
            builder.setQuitUserId((Integer)userId);

            GameMsgProtocol.UserQuitResult result = builder.build();

            Broadcaster.broadcast(result);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.info("客户端消息：" + msg.getClass() + ", msg: " + msg.toString());

        if(!(msg instanceof GeneratedMessageV3)) {
            return;
        }

        ICmdHandler handler = CmdHandlerFactory.create(msg.getClass());

        if(handler != null) {
            handler.handle(ctx, cast(msg));
        }
    }

    private static <TCmd extends GeneratedMessageV3> TCmd cast(Object msg) {
        if(msg == null || !(msg instanceof GeneratedMessageV3)) {
            return null;
        }
        return (TCmd)msg;
    }
}
