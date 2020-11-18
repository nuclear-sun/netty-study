package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.Broadcaster;
import org.sun.herostory.exception.AuthFailedException;
import org.sun.herostory.login.LoginService;
import org.sun.herostory.login.db.UserEntity;
import org.sun.herostory.model.User;
import org.sun.herostory.model.UserManager;
import org.sun.herostory.msg.GameMsgProtocol;

public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocol.UserLoginCmd> {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext context, GameMsgProtocol.UserLoginCmd msg) {

        if(context == null || msg == null) {
            return;
        }

        UserEntity userEntity = null;

        try {
            userEntity = LoginService.getInstance().userLogin(msg.getUserName(), msg.getPassword());
        } catch (AuthFailedException e) {
            logger.error("Auth failed with userName: {}, password: {}", msg.getUserName(), msg.getPassword());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if(userEntity == null) {
            return;
        }

        // 添加到用户管理
        User user = new User();
        user.setUserId(userEntity.getUserId());
        user.setCurrHp(100);
        user.setHeroAvatar(userEntity.getHeroAvatar());
        UserManager.addUser(user);

        // 附属管道信息
        context.channel().attr(AttributeKey.valueOf("userId")).set(user.getUserId());

        // 添加到广播管理器中
        Broadcaster.addChannel(context.channel());

        // 返回登录成功消息
        GameMsgProtocol.UserLoginResult.Builder builder = GameMsgProtocol.UserLoginResult.newBuilder();
        builder.setUserId(userEntity.getUserId());
        builder.setUserName(userEntity.getUserName());
        builder.setHeroAvatar(userEntity.getHeroAvatar());
        GameMsgProtocol.UserLoginResult userLoginResult = builder.build();
        context.writeAndFlush(userLoginResult);
    }
}
