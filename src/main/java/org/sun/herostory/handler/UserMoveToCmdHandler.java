package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.sun.herostory.Broadcaster;
import org.sun.herostory.model.MoveState;
import org.sun.herostory.model.User;
import org.sun.herostory.model.UserManager;
import org.sun.herostory.msg.GameMsgProtocol;

public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd> {

    public void handle(ChannelHandlerContext context, GameMsgProtocol.UserMoveToCmd msg) {

        Integer userId =(Integer) context.channel().attr(AttributeKey.valueOf("userId")).get();
        if(userId == null) {
            return;
        }

        GameMsgProtocol.UserMoveToCmd cmd = (GameMsgProtocol.UserMoveToCmd) msg;

        float moveFromPosX = cmd.getMoveFromPosX();
        float moveFromPosY = cmd.getMoveFromPosY();
        float moveToPosX = cmd.getMoveToPosX();
        float moveToPosY = cmd.getMoveToPosY();
        long startTime = System.currentTimeMillis();

        // 更新管理的用户状态，用于 WhoElseIsHere 查询
        User user = UserManager.getUserById(userId);
        MoveState moveState = user.getMoveState();
        moveState.setFromPosX(moveFromPosX);
        moveState.setFromPosY(moveFromPosY);
        moveState.setToPosX(moveToPosX);
        moveState.setToPosY(moveToPosY);
        moveState.setStartTime(startTime);

        // 广播移动消息
        GameMsgProtocol.UserMoveToResult.Builder builder = GameMsgProtocol.UserMoveToResult.newBuilder();
        builder.setMoveFromPosX(moveFromPosX);
        builder.setMoveFromPosY(moveFromPosY);
        builder.setMoveToPosX(moveToPosX);
        builder.setMoveToPosY(moveToPosY);
        builder.setMoveStartTime(startTime);
        builder.setMoveUserId(userId);

        GameMsgProtocol.UserMoveToResult result = builder.build();

        Broadcaster.broadcast(result);
    }
}
