package org.sun.herostory.login.db;

import org.apache.ibatis.annotations.Param;

public interface IUserDao {

    UserEntity getByUserName(@Param("userName") String userName);

    void insertInto(UserEntity userEntity);
}
