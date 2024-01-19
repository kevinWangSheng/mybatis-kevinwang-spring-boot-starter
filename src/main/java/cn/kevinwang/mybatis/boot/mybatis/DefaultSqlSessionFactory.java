package cn.kevinwang.mybatis.boot.mybatis;

/**
 * @author wang
 * @create 2024-01-19-14:27
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory{
    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration.getConnection(),configuration.getMapperElement());
    }
}
