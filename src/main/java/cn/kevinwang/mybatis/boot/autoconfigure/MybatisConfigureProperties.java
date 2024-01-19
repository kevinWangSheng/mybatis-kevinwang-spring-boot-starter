package cn.kevinwang.mybatis.boot.autoconfigure;

import cn.kevinwang.mybatis.boot.common.Constance;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wang
 * @create 2024-01-19-14:35
 */
@ConfigurationProperties(prefix = Constance.MybatisConfig.PROPERTIES_PREFIX)
public class MybatisConfigureProperties {

    private String driver;

    private String url;

    private String username;

    private String password;

    private String mapperLocation;

    private String baseDaoPackage;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMapperLocation() {
        return mapperLocation;
    }

    public void setMapperLocation(String mapperLocation) {
        this.mapperLocation = mapperLocation;
    }

    public String getBaseDaoPackage() {
        return baseDaoPackage;
    }

    public void setBaseDaoPackage(String baseDaoPackage) {
        this.baseDaoPackage = baseDaoPackage;
    }
}
