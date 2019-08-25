package com.lsj.java.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.lsj.java.util.CommUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class JDBCTest {
    private static DruidDataSource dataSource;
    static {
        Properties properties = CommUtils.loadProperties("datasource.properties");
        try{
            dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * PreparedStatement 会防止SQL注入，
     * 尽管test' -- ，也会认为整体是用户名，不会做特殊化处理
     * 而Statement并不会做检查
     */
    @Test
    public void testQuery(){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            connection = (Connection) dataSource.getPooledConnection();
            String sql = "select * from user where username = ? and password = ?";
            statement = connection.prepareStatement(sql);
            String user = "test";
            String pass = "123";
            statement.setString(1, user);
            statement.setString(2, pass);
            resultSet = statement.executeQuery();
//            while(resultSet.next()){
//                int id = resultSet.getInt("id");
//                String username = resultSet.getString("username");
//                String password = resultSet.getString("password");
//                String brief = resultSet.getString("brief");
//                System.out.println("id : " + id + " ; username : " + username + " ; password : " + password + " ; brief : " + brief);
//            }
            if(resultSet.next()){
                System.out.println("OK");
            }else{
                System.out.println("NO");
            }
        }catch (SQLException e){

        }finally {
            closeResources(connection, statement, resultSet);
        }
    }

    @Test
    public void testInsert(){
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            connection = (Connection) dataSource.getPooledConnection();
            String password = DigestUtils.md5Hex("123");
            String sql = "insert into user(username, password, brief) values (?,?,?)";
            statement  = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "test1");
            statement.setString(2, password);
            statement.setString(3, "还是帅");
            int rows = statement.executeUpdate();
            Assert.assertEquals(1,rows);
        }catch (SQLException e){

        }finally {
            closeResources(connection, statement);
        }
    }

    @Test
    public void testLogin(){
        String userName = "test";
        String password = "123";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            connection = (Connection) dataSource.getPooledConnection();
            String sql = "select * from user where username = '"+userName+"' and password = '"+password+"'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                System.out.println("OK");
            }else{
                System.out.println("NO");
            }
        }catch (SQLException e){

        }finally {
            closeResources(connection, statement, resultSet);
        }
    }

    public void closeResources(Connection connection, Statement statement){
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(statement != null){
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void closeResources(Connection connection, Statement statement, ResultSet resultSet){
        closeResources(connection, statement);
        if(resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}