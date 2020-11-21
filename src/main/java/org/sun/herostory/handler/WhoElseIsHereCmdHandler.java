package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import org.sun.herostory.model.MoveState;
import org.sun.herostory.model.User;
import org.sun.herostory.model.UserManager;
import org.sun.herostory.msg.GameMsgProtocol;

public class WhoElseIsHereCmdHandler implements ICmdHandler<GameMsgProtocol.WhoElseIsHereCmd> {

    public void handle(ChannelHandlerContext context, GameMsgProtocol.WhoElseIsHereCmd msg) {

        GameMsgProtocol.WhoElseIsHereResult.Builder builder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();

        for (User user : UserManager.listUsers()) {
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuilder.setUserId(user.getUserId());
            userInfoBuilder.setUserName(user.getUserName());
            userInfoBuilder.setHeroAvatar(user.getHeroAvatar());

            // 获取移动状态
            MoveState moveState = user.getMoveState();
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.Builder moveStateBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            moveStateBuilder.setFromPosX(moveState.getFromPosX());
            moveStateBuilder.setFromPosY(moveState.getFromPosY());
            moveStateBuilder.setToPosX(moveState.getToPosX());
            moveStateBuilder.setToPosY(moveState.getToPosY());
            moveStateBuilder.setStartTime(moveState.getStartTime());
            userInfoBuilder.setMoveState(moveStateBuilder.build());
            builder.addUserInfo(userInfoBuilder);
        }

        GameMsgProtocol.WhoElseIsHereResult result = builder.build();
        context.channel().writeAndFlush(result);
    }
}
