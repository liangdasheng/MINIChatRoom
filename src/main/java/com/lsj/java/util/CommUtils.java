package com.lsj.java.util;
/**
 * 封装公共工具类方法，如：加载配置文件、json序列化等
 * ctrl+shift+T : 单元测试测试类
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CommUtils {
    /**
     * 加载配置文件
     * @param fileName
     * @return
     */
    private static final Gson GSON = new GsonBuilder().create();
    public static Properties loadProperties(String fileName){
        Properties properties = new Properties();
        //只要资源文件和该类在同一路径下，类加载器可以加载该文件并将其变为输入流
        //即：java与resources在同一路径下
        InputStream in = CommUtils.class.getClassLoader().getResourceAsStream(fileName);
        try{
            properties.load(in);
        }catch (IOException e){
            //e.printStackTrace();
            return null;
        }
        return properties;
    }

    /**
     * 将任意对象序列化为json字符串
     * @param obj
     * @return
     */
    public static String object2Json(Object obj){
        return GSON.toJson(obj);
    }
    /**
     * 将任意json字符串反序列化为obj对象
     * objClass就是反序列化的类反射对象
     */
    public static Object json2Object(String jsonStr, Class objClass){
        return GSON.fromJson(jsonStr, objClass);
    }
}
