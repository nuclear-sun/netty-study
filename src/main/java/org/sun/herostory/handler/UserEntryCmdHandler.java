package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.Broadcaster;
import org.sun.herostory.model.User;
import org.sun.herostory.model.UserManager;
import org.sun.herostory.msg.GameMsgProtocol;

public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocol.UserEntryCmd> {

    private static final Logger logger = LoggerFactory.getLogger(UserEntryCmdHandler.class);

    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserEntryCmd cmd) {

        if(ctx == null || cmd == null) {
            return;
        }

        Object userId = ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if(userId == null) {
            return;
        }
        User user = UserManager.getUserById((Integer) userId);

        if(user == null) {
            logger.error("未找到 {} 对应的用户！", userId);
            return;
        }

        // 通知
        GameMsgProtocol.UserEntryResult.Builder builder = GameMsgProtocol.UserEntryResult.newBuilder();

        builder.setUserId(user.getUserId());
        builder.setHeroAvatar(user.getHeroAvatar());
        builder.setUserName(user.getUserName());
        GameMsgProtocol.UserEntryResult result = builder.build();
        Broadcaster.broadcast(result);
    }
}
