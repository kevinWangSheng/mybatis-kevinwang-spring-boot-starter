## 简易mybatis
主要仿照mybaits封装了sql session执行一些sql语句

主要包含dao解析，以及对应的xml解析，然后在用dom4j进行获取sql语句，并使用正则表达式进行提取参数重新封装参数，最后处理返回的结果

如下扫描xml：
```java

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
```

这里的basePackage就是我们放置mapper.xml的地方，扫描该目录下面的xml文件，然后进行解析，放到内存中

解析dao的class文件如下：
```java
private String basePackage;
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            // 扫描该包下面的class文件
            String packageSearchPath = "classpath*:" + this.basePackage.replace(".", "/") + "/**/*.class";

            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                MetadataReader metadataReader = new SimpleMetadataReaderFactory().getMetadataReader(resource);

                ScannedGenericBeanDefinition beanDefinition = new ScannedGenericBeanDefinition(metadataReader);
                String beanName = Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));

                beanDefinition.setResource(resource);
                beanDefinition.setSource(resource);
                beanDefinition.setScope("singleton");
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
                beanDefinition.setBeanClass(MapperFactoryBean.class);

                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
                registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
```

配置的基本信息如下：
```java
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
```

获取连接
```java
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
```
然后使用工厂模式创建sqlsession，这个sqlsession是自己定义的，内部主要进行处理查询sql等操作。
```java
@Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(Connection connection) throws Exception {
        return new SqlSessionFactoryBuilder().build(connection, properties.getBaseDaoPackage());
    }
```

类似封装如下的方法：
```java
public interface SqlSession {
    <T> T selectOne(String statement);

    <T> T selectOne(String statement, Object parameter);

    <T> List<T> selectList(String statement);

    <T> List<T> selectList(String statement, Object parameter);

    void close();
}
```

这里执行的dao方法都用了jdk进行代理执行的，然后在启动的时候注册这个bean