package org.sun.herostory.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.Broadcaster;
import org.sun.herostory.async.IAsyncOperation;
import org.sun.herostory.async.PooledAsyncProcessor;
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

        if(msg.getUserName() == null || msg.getPassword() == null) {
            logger.error("用户名或密码为空！");
            return;
        }

        LoginOperation loginOperation = new LoginOperation(msg.getUserName(), msg.getPassword()) {
            @Override
            public void finish() {
                UserEntity userEntity = getUserEntity();
                if(userEntity == null) {
                    return;
                }

                // 添加到用户管理
                User user = new User();
                user.setUserId(userEntity.getUserId());
                user.setUserName(userEntity.getUserName());
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
        };

        PooledAsyncProcessor.getInstance().process(loginOperation);
    }


    private static class LoginOperation implements IAsyncOperation {

        private final String userName;
        private final String password;

        private volatile UserEntity userEntity;

        public LoginOperation(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public int bind() {
            return userName.hashCode();
        }

        @Override
        public void doAsync() {

            try {
                userEntity = LoginService.getInstance().userLogin(userName, password);
            } catch (AuthFailedException e) {
                logger.error("Auth failed with userName: {}, password: {}", userName, password);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        // Intended to be called from a different thread
        protected UserEntity getUserEntity() {
            return this.userEntity;
        }
    }
}
