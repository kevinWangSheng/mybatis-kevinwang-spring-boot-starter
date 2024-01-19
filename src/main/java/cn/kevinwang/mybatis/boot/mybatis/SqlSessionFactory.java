package cn.kevinwang.mybatis.boot.mybatis;

/**
 * @author wang
 * @create 2024-01-19-14:29
 */
public interface SqlSessionFactory {
    SqlSession openSession();
}
