package cn.kevinwang.mybatis.boot.mybatis;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.xml.sax.InputSource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wang
 * @create 2024-01-19-14:44
 */
public class SqlSessionFactoryBuilder {
    // 主要用于解析xml配置文件,并且构建SqlSessionFactory
    public DefaultSqlSessionFactory build(Connection connection, String basePackage) throws Exception{
        Configuration configuration = new Configuration();
        configuration.setConnection(connection);

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources(basePackage);

        List<Element> list = new ArrayList<>(resources.length);
        // 对该路径下的每一个资源进行解析
        for(Resource resource:resources){
            Document document = new SAXReader().read(new InputSource(resource.getInputStream()));
            list.add(document.getRootElement());
        }
        configuration.setMapperElement(mapperElement(list));
        return new DefaultSqlSessionFactory(configuration);
    }

    private Map<String, XNode> mapperElement(List<Element> list) {
        Map<String,XNode> map = new HashMap<>();
        for(Element element:list){
            String namespace = element.attributeValue("namespace");

            List<Element> select = element.selectNodes("select");
            // 对一个sql方法进行解析
            for(Element node:select){
                String id = node.attributeValue("id");
                String parameterType = node.attributeValue("parameterType");
                String resultType = node.attributeValue("resultType");
                String sql = node.getText();
                // 对参数进行封装
                Map<Integer,String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile("(#\\{(.*?)})");
                Matcher matcher = pattern.matcher(sql);
                for(int i = 1;matcher.find();i++){
                    String all = matcher.group(1);
                    String param = matcher.group(2);
                    parameter.put(i,param);
                    // 对其中的参数进行替换
                    sql = sql.replace(all,"?");
                }

                XNode xNode = new XNode();
                xNode.setSql(sql);
                xNode.setId(id);
                xNode.setNamespace(namespace);
                xNode.setResultType(resultType);
                xNode.setParameterType(parameterType);
                xNode.setParameter(parameter);

                map.put(namespace+"."+id,xNode);
            }
        }
        return map;
    }
}
