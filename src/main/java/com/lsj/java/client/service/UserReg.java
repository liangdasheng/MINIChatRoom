package com.lsj.java.client.service;

import com.lsj.java.client.dao.AccountDao;
import com.lsj.java.client.entity.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserReg {

    private JPanel userRegPanel;
    private JTextField userNameTest;
    private JPasswordField passwordText;
    private JTextField briefText;
    private JButton regBtn;
    private AccountDao accountDao = new AccountDao();

    public UserReg(){
        JFrame frame = new JFrame("用户注册");
        frame.setContentPane(userRegPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        //点击注册按钮，会将信息持久化到database中，成功弹出提示框
        //监听
        regBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //获取文本框中输入的内容
                String userName = userNameTest.getText();
                String password = String.valueOf(passwordText.getPassword());
                String brief = briefText.getText();
                //将输入信息包装为User类，保存到数据库中
                User user = new User();
                user.setUserName(userName);
                user.setPassword(password);
                user.setBrief(brief);
                //调用Dao对象，根据返回值可以知道是注册成功还是失败
                //模拟弹出提示框，类JOptionPane
                //JOptionPane.INFORMATION_MESSAGE  成功信息
                //JOptionPane.ERROR_MESSAGE  错误信息
                if(accountDao.userReg(user)){
                    //注册成功，返回登陆页面，注册页面默认关闭
                    JOptionPane.showMessageDialog(frame, "注册成功！", "提示信息",
                            JOptionPane.INFORMATION_MESSAGE);
                    //当前页面不可见，否则失败还是保留当前页面
                    frame.setVisible(false);
                }else{
                    //注册失败，保留当前页面，提示用户重新输入信息
                    JOptionPane.showMessageDialog(frame, "注册失败！", "错误信息",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
