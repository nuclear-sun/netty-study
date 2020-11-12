package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.sun.herostory.Broadcaster;
import org.sun.herostory.model.User;
import org.sun.herostory.model.UserManager;
import org.sun.herostory.msg.GameMsgProtocol;

public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocol.UserEntryCmd> {

    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserEntryCmd cmd) {

        if(ctx == null || cmd == null) {
            return;
        }

        int userId = cmd.getUserId();

        // 通知
        GameMsgProtocol.UserEntryResult.Builder builder = GameMsgProtocol.UserEntryResult.newBuilder();
        builder.setUserId(userId);
        builder.setHeroAvatar(cmd.getHeroAvatar());
        GameMsgProtocol.UserEntryResult result = builder.build();

        User user = new User();
        user.setUserId(cmd.getUserId());
        user.setHeroAvatar(cmd.getHeroAvatar());
        user.setCurrHp(100);

        UserManager.addUser(user);

        ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);

        Broadcaster.broadcast(result);
    }
}
