package com.lsj.java.client.dao;

import com.lsj.java.client.entity.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;

public class AccountDao extends BasedDao{
    //用户注册
    public boolean userReg(User user){
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            connection = getConnection();
            String sql = "insert into user(username, password, brief) values (?,?,?)";
            //Statement.RETURN_GENERATED_KEYS  受影响的行数
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1,user.getUserName());
            statement.setString(2, DigestUtils.md5Hex(user.getPassword()));
            statement.setString(3, user.getBrief());
            int rows = statement.executeUpdate();
            if(rows == 1){
                return true;
            }
        }catch (SQLException e){
            System.out.println("用户注册失败");
            e.printStackTrace();
        }finally {
            closeResources(connection, statement);
        }
        return false;
    }
    //用户登录和注册是两个事务，如果是一个事务，当一个还没有注册，另一个已经登陆，当然就不行
    //所以必须是隔离的，所以每次都需要connection = getConnection();
    public User userLogin(String userName, String password){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            connection = getConnection();
            String sql = "select * from user where username = ? and password = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1,userName);
            statement.setString(2, DigestUtils.md5Hex(password));
            resultSet = statement.executeQuery();
            if(resultSet.next()){
                User user = getUser(resultSet);
                return user;
            }
        }catch (SQLException e){
            System.err.println("用户登陆失败");
            e.printStackTrace();
        }finally {
            closeResources(connection, statement, resultSet);
        }
        return null;
    }
    private User getUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUserName(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setBrief(resultSet.getString("brief"));
        return user;
    }
}
