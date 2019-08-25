package com.lsj.java.vo;

import lombok.Data;

/**
 * 服务器与客户端传递信息的载体
 *
 */
@Data
public class MessageVO {
    /**
     * 告知服务器要进行的动作
     */
    private String type;
    /**
     * 具体内容
     */
    private String content;
    /**
     * 私聊告知服务器将信息发送给谁
     */
    private String to;
}
