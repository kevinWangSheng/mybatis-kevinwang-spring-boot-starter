package cn.kevinwang.mybatis.boot.mybatis;

import org.dom4j.Element;

import java.sql.Connection;
import java.util.Map;

/**
 * @author wang
 * @create 2024-01-19-14:26
 */
public class Configuration {
    private Connection connection;

    private Map<String,String> dataSource;

    private Map<String, XNode> mapperElement;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
    }

    public void setMapperElement(Map<String, XNode> mapperElement) {
        this.mapperElement = mapperElement;
    }

    public Connection getConnection() {
        return connection;
    }

    public Map<String, String> getDataSource() {
        return dataSource;
    }

    public Map<String, XNode> getMapperElement() {
        return mapperElement;
    }
}
