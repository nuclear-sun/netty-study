package org.sun.herostory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlSessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(MySqlSessionFactory.class);

    private MySqlSessionFactory() {}

    private static SqlSessionFactory factory;

    static {

        SqlSession sqlSession = null;
        try {
            factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream("MyBatisConfig.xml"));
            // 测试
            sqlSession = factory.openSession();
            sqlSession.getConnection().createStatement().execute("SELECT 1");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if(sqlSession != null) {
                sqlSession.close();
            }
        }
    }

    public static SqlSession openSession() {

        if(factory == null) {
            throw new RuntimeException("sqlsessionfactory 未初始化！");
        }
        return factory.openSession(true);
    }

}
