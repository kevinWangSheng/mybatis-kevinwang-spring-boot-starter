package cn.kevinwang.mybatis.boot.autoconfigure;

import cn.kevinwang.mybatis.boot.mybatis.DefaultSqlSessionFactory;
import cn.kevinwang.mybatis.boot.mybatis.SqlSession;
import cn.kevinwang.mybatis.boot.mybatis.SqlSessionFactory;
import cn.kevinwang.mybatis.boot.mybatis.SqlSessionFactoryBuilder;
import cn.kevinwang.mybatis.boot.spring.MapperFactoryBean;
import cn.kevinwang.mybatis.boot.spring.MapperScannerConfig;
import com.mysql.cj.jdbc.MysqlDataSource;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author wang
 * @create 2024-01-19-14:38
 */
@AutoConfiguration
@ConditionalOnClass(SqlSession.class)
@EnableConfigurationProperties(MybatisConfigureProperties.class)
public class MybatisAutoConfigure {
    @Resource
    private MybatisConfigureProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public Connection connection(){
        try {
            Class.forName(properties.getDriver());
            return DriverManager.getConnection(properties.getUrl(),properties.getUsername(),properties.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 对扫描dao的包资源的类进行注册
    public static class AutoConfiguredMapperScannerRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {

        private String basePackage;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfig.class);
            builder.addPropertyValue("basePackage", basePackage);
            registry.registerBeanDefinition(MapperScannerConfig.class.getName(), builder.getBeanDefinition());
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.basePackage = environment.getProperty("mybatis.datasource.base-dao-package");
        }
    }

    @Configuration
    @Import(AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnMissingBean({MapperFactoryBean.class, MapperScannerConfig.class})
    public static class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {

        @Override
        public void afterPropertiesSet() {
        }

    }



    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(Connection connection) throws Exception {
        return new SqlSessionFactoryBuilder().build(connection, properties.getBaseDaoPackage());
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSession sqlSession(SqlSessionFactory sqlSessionFactory){
        return sqlSessionFactory.openSession();
    }
}
