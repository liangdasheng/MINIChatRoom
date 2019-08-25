package com.lsj.java.client.service;

import com.lsj.java.util.CommUtils;
import com.lsj.java.vo.MessageVO;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class PrivateChatGUI {
    private JPanel privateChatPanel;
    private JTextArea readFromServer;
    private JTextField send2Server;

    private String friendName;
    private String myName;
    private Connect2Server connect2Server;
    private JFrame frame;
    private PrintStream out;

    public PrivateChatGUI(String friendName, String myName, Connect2Server connect2Server){
        this.friendName = friendName;
        this.myName = myName;
        this.connect2Server = connect2Server;
        try {
            this.out = new PrintStream(connect2Server.getOut(),true,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        frame = new JFrame("与" + friendName + "私聊中...");
        frame.setContentPane(privateChatPanel);
        //setDefaultCloseOperation  关闭窗口，并不会把程序关闭，只需要隐藏HIDE_ON_CLOSE
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //frame.pack();
        frame.setSize(400,400);
        frame.setVisible(true);
        //捕捉输入框的键盘输入，模拟QQ，没有发送按钮，直接回车可以发消息
        //KeyAdapter是一个抽象类，不能new，所以里面实际上是一个匿名内部类
        send2Server.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append(send2Server.getText());
                //当捕捉到按下回车键，将当前信息发送到服务器，将自己发送的信息展示到当前私聊页面
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    String msg = sb.toString();
                    MessageVO messageVO = new MessageVO();
                    messageVO.setType("2");
                    messageVO.setContent(myName + "-" + msg);
                    messageVO.setTo(friendName);
                    //获取外部类PrivateChatGUI
                    PrivateChatGUI.this.out.println( CommUtils.object2Json(messageVO));
                    readFromServer(myName + "说：" + msg);
                    //Text置为空，表示输入框还原
                    send2Server.setText("");
                }
            }
        });
    }
    //将自己的信息展示到当前页面
    public void readFromServer(String msg){
        readFromServer.append(msg + "\n");
    }

    public JFrame getFrame(){
        return frame;
    }
}
