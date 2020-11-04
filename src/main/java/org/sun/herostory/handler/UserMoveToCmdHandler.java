package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.sun.herostory.Broadcaster;
import org.sun.herostory.msg.GameMsgProtocol;

public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd> {

    public void handle(ChannelHandlerContext context, GameMsgProtocol.UserMoveToCmd msg) {

        Integer userId =(Integer) context.channel().attr(AttributeKey.valueOf("userId")).get();
        if(userId == null) {
            return;
        }

        GameMsgProtocol.UserMoveToCmd cmd = (GameMsgProtocol.UserMoveToCmd) msg;
        GameMsgProtocol.UserMoveToResult.Builder builder =  GameMsgProtocol.UserMoveToResult.newBuilder();
        builder.setMoveToPosX(cmd.getMoveToPosX());
        builder.setMoveToPosY(cmd.getMoveToPosY());
        builder.setMoveUserId(userId);

        GameMsgProtocol.UserMoveToResult result = builder.build();

        Broadcaster.broadcast(result);
    }
}
