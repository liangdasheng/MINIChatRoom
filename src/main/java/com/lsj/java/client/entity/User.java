package com.lsj.java.client.entity;

import lombok.Data;

/**
 * Integer默认值null，int默认为0，
 * 如果设置id not null，当用户没有插入时，Integer就会报错，提示用户插入数据，
 * 如果是int，当用户没有插入数据时i，就是0，表示插入的数据为0，不会报错
 */
@Data
public class User {
    private Integer id;
    private String userName;
    private String password;
    private String brief;
}
