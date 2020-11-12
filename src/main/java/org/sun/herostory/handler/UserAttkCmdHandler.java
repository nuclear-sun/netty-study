package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.sun.herostory.Broadcaster;
import org.sun.herostory.model.User;
import org.sun.herostory.model.UserManager;
import org.sun.herostory.msg.GameMsgProtocol;

public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {

    @Override
    public void handle(ChannelHandlerContext context, GameMsgProtocol.UserAttkCmd msg) {

        Object userId = context.channel().attr(AttributeKey.valueOf("userId")).get();
        if(userId == null) {
            return;
        }

        // 攻击结果
        GameMsgProtocol.UserAttkResult.Builder builder = GameMsgProtocol.UserAttkResult.newBuilder();
        builder.setAttkUserId((Integer)userId);
        builder.setTargetUserId(msg.getTargetUserId());
        GameMsgProtocol.UserAttkResult result = builder.build();
        Broadcaster.broadcast(result);

        // 掉血结果
        User targetUser = UserManager.getUserById(msg.getTargetUserId());
        if(targetUser == null) {
            return;
        }
        int subtractHp = 10;
        targetUser.setCurrHp(targetUser.getCurrHp() - subtractHp);
        broadcastSubtractHp(targetUser.getUserId(), subtractHp);

        // 死亡结果
        if(targetUser.getCurrHp() <= 0) {
            GameMsgProtocol.UserDieResult.Builder userDieBuilder = GameMsgProtocol.UserDieResult.newBuilder();
            userDieBuilder.setTargetUserId(targetUser.getUserId());
            Broadcaster.broadcast(userDieBuilder.build());
        }
    }

    private void broadcastSubtractHp(int userId, int subtractHp) {
        if(userId < 0 || subtractHp <= 0) {
            return;
        }
        GameMsgProtocol.UserSubtractHpResult.Builder builder = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        builder.setTargetUserId(userId);
        builder.setSubtractHp(subtractHp);
        GameMsgProtocol.UserSubtractHpResult result = builder.build();
        Broadcaster.broadcast(result);
    }
}
