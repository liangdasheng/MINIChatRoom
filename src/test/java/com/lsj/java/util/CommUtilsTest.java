package com.lsj.java.util;

import com.lsj.java.client.entity.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class CommUtilsTest {

    /**
     * 单元测试
     */
    @Test
    public void loadProperties() {
        String fileName = "datasource.properties";
        Properties properties = CommUtils.loadProperties(fileName);
        //System.out.println(properties);
        Assert.assertNotNull(properties);
    }

    @Test
    public void object2Json() {
        User user = new User();
        user.setId(1);
        user.setUserName("test");
        user.setPassword("123");
        user.setBrief("帅");
        String str = CommUtils.object2Json(user);
        System.out.println(str);
    }

    @Test
    public void json2Object() {
        String jsonStr = "{\"id\":1,\"userName\":\"test\",\"password\":\"123\",\"brief\":\"帅\"}";
        User user = (User) CommUtils.json2Object(jsonStr, User.class);
        System.out.println(user.getUserName());
    }
}