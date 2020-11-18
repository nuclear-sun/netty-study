package org.sun.herostory.login;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.MySqlSessionFactory;
import org.sun.herostory.exception.AuthFailedException;
import org.sun.herostory.login.db.IUserDao;
import org.sun.herostory.login.db.UserEntity;

public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private LoginService() {}

    private static final LoginService instance = new LoginService();

    public final static LoginService getInstance() {
        return instance;
    }


    public UserEntity userLogin(String userName, String password) {

        if (userName == null || password == null) {
            return null;
        }

        try (SqlSession sqlSession = MySqlSessionFactory.openSession()) {
            if (sqlSession == null) {
                throw new RuntimeException("获取 sqlSession 异常！");
            }

            IUserDao mapper = sqlSession.getMapper(IUserDao.class);
            if (mapper == null) {
                throw new RuntimeException("获取 IUserDao 实例失败！");
            }

            UserEntity userEntity = mapper.getByUserName(userName);

            if (userEntity == null) {
                userEntity = new UserEntity();
                userEntity.setUserName(userName);
                userEntity.setPassword(password);
                userEntity.setHeroAvatar("Hero_Shaman");

                mapper.insertInto(userEntity);
                return userEntity;

            } else {
                if (password.equals(userEntity.getPassword())) {
                    return userEntity;
                } else {
                    throw new AuthFailedException(userName + ":" + password);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
