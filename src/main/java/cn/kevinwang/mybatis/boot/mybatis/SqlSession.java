package cn.kevinwang.mybatis.boot.mybatis;

import java.util.List;

/**
 * @author wang
 * @create 2024-01-19-14:29
 */
public interface SqlSession {
    <T> T selectOne(String statement);

    <T> T selectOne(String statement, Object parameter);

    <T> List<T> selectList(String statement);

    <T> List<T> selectList(String statement, Object parameter);

    void close();
}
