package cn.kevinwang.mybatis.boot.mybatis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * @author wang
 * @create 2024-01-19-14:27
 */
public class DefaultSqlSession implements SqlSession{
    private Connection connection;

    private Map<String,XNode> mapperElement;

    public DefaultSqlSession(Connection connection, Map<String,XNode> mapperElement){
        this.connection = connection;
        this.mapperElement = mapperElement;
    }
    @Override
    public <T> T selectOne(String statement) {
        try {
            XNode xNode = mapperElement.get(statement);
            if(null == xNode){
                throw new RuntimeException("no this method ");
            }
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = resultSet2Obj(resultSet, xNode.getResultType().getClass());
            return objects.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public <T> T selectOne(String statement, Object parameter) {
        try {
            XNode xNode = mapperElement.get(statement);
            Map<Integer, String> parameterMap = xNode.getParameter();
            if(null == xNode){
                throw  new RuntimeException("no this method");
            }
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            buildParameter(preparedStatement,parameter,parameterMap);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet2Obj(resultSet,xNode.getResultType().getClass()).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public <T> List<T> selectList(String statement) {
        try {
            XNode xNode = mapperElement.get(statement);
            if(null == xNode){
                return null;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, xNode.getResultType().getClass());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        try {
            XNode xNode = mapperElement.get(statement);
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            buildParameter(preparedStatement,xNode.getParameter(),xNode.getParameter());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, xNode.getResultType().getClass());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        if(null ==connection) return;
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildParameter(PreparedStatement preparedStatement, Object parameter, Map<Integer, String> parameterMap) {
        int size = parameterMap.size();

        // 如果是单个参数
        try {
            if(parameter instanceof  Long){
                for(int i = 1;i<=size;i++){
                    preparedStatement.setLong(i,Long.parseLong(parameter.toString()));
                }
                return;
            }
            if(parameter instanceof Integer){
                for(int i = 1;i<=size;i++){
                    preparedStatement.setInt(i,Integer.parseInt(parameter.toString()));
                }
                return ;
            }
            if(parameter instanceof String){
                for(int i = 1;i<=size;i++){
                    preparedStatement.setString(i,parameter.toString());
                }
                return;
            }
            Map<String, Object> fieldMap = new HashMap<>();
            Field[] fields = parameter.getClass().getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();
                field.setAccessible(true);
                Object obj = field.get(parameter);
                field.setAccessible(false);
                fieldMap.put(name,obj);
            }

            for(int i = 1;i<=size;i++){
                String parameterObj = parameterMap.get(i);
                Object parameterValue = fieldMap.get(parameterObj);

                if(parameterValue instanceof Long){
                    preparedStatement.setLong(i,Long.parseLong(parameterValue.toString()));
                }else if(parameterValue instanceof Short){
                    preparedStatement.setShort(i,Short.parseShort(parameterValue.toString()));
                    continue;
                }else if(parameterValue instanceof Integer){
                    preparedStatement.setInt(i,Integer.parseInt(parameterValue.toString()));
                }else if(parameterValue instanceof String){
                    preparedStatement.setString(i,parameterValue.toString());
                }else if(parameterValue instanceof Date){
                    preparedStatement.setDate(i,(java.sql.Date)parameterValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<? extends String> clazz) {
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 对结果进行一个一个解析封装
            while(resultSet.next()){
                T t = (T) clazz.newInstance();
                for(int i = 1;i<=columnCount;i++){
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    String methodName = "set"+columnName.substring(0,1).toUpperCase()+columnName.substring(1);
                    Method method;
                    // 这里调用对应的set方法，上面对那个方法名字的属性的第一个字母变成大写，然后调用方法
                    if(value instanceof Timestamp){
                        method = clazz.getMethod(methodName, Date.class);
                    }else{
                        method = clazz.getMethod(methodName,value.getClass());
                    }
                    method.invoke(t,value);
                }
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
