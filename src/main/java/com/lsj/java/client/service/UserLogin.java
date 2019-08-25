package com.lsj.java.client.service;

import com.lsj.java.client.dao.AccountDao;
import com.lsj.java.client.entity.User;
import com.lsj.java.util.CommUtils;
import com.lsj.java.vo.MessageVO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Set;

public class UserLogin {
    private JPanel userLogin;
    private JPanel userPanel;
    private JTextField userNameText;
    private JPasswordField passwordText;
    private JPanel btnPanel;
    private JButton regButton;
    private JButton loginButton;
    private JFrame frame;
    private AccountDao accountDao = new AccountDao();

    public UserLogin() {
        JFrame frame = new JFrame("用户登录");
        frame.setContentPane(userLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        //注册按钮
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //弹出注册页面
                new UserReg();
            }
        });
        //登录按钮
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //校验用户信息
                String userName = userNameText.getText();
                String password = String.valueOf(passwordText.getPassword());
                User user = accountDao.userLogin(userName, password);
                if(user != null){
                    //成功，加载好友列表
                    JOptionPane.showMessageDialog(frame,"登陆成功！", "提示信息",
                            JOptionPane.INFORMATION_MESSAGE);
                    frame.setVisible(false);
                    //与服务器建立连接，将当前用户的用户名与密码发送到服务器
                    //type : 告诉服务器发送的类型
                    //content : 发送的具体内容
                    //to : 告诉服务器给谁发
                    Connect2Server connect2Server = new Connect2Server();
                    MessageVO msg2Server = new MessageVO();
                    msg2Server.setType("1");
                    msg2Server.setContent(userName);
                    String json2Server = CommUtils.object2Json(msg2Server);
                    try {
                        PrintStream out = new PrintStream(connect2Server.getOut(), true, "UTF-8");
                        out.println(json2Server);
                        //读取服务端发回的所有在线用户信息
                        Scanner in = new Scanner(connect2Server.getIn());
                        if(in.hasNextLine()){
                            String msgFromServerstr = in.nextLine();
                            MessageVO msgFromServer = (MessageVO) CommUtils.json2Object(msgFromServerstr, MessageVO.class);
                            Set<String> users = (Set<String>) CommUtils.json2Object(msgFromServer.getContent(), Set.class);
                            System.out.println("所有在线用户为：" + users);
                            //加载用户列表界面
                            //将当前用户名、所有在线好友、与服务器建立连接传递到好友列表界面
                            //通过构造方法传递
                            new FriendsList(userName, users, connect2Server);
                        }
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                }else{
                    //失败，停留在登陆页面
                    JOptionPane.showMessageDialog(frame, "登陆失败！", "错误信息",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        UserLogin userLogin = new UserLogin();
    }
}
