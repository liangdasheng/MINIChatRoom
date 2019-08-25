package com.lsj.java.client.service;

import com.lsj.java.util.CommUtils;
import com.lsj.java.vo.MessageVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreateGroupGUI {
    private JPanel createGroupPanel;
    private JPanel friendLabelPanel;
    private JTextField groupNameText;
    private JButton conformBtn;

    private String myName;
    private Set<String> friends;
    private Connect2Server connect2Server;
    private FriendsList friendsList;

    public CreateGroupGUI(String myName, Set<String> friends,
                          Connect2Server connect2Server,FriendsList friendsList) {
        this.myName = myName;
        this.friends = friends;
        this.connect2Server = connect2Server;
        this.friendsList = friendsList;
        JFrame frame = new JFrame("创建群组");
        frame.setContentPane(createGroupPanel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(400,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //首先加载好友列表，将在线好友以checkBox的方式展示到界面中
        //checkBox 可以勾选，选择某些好友创建群聊
        //设置其展示方式是纵向展示
        friendLabelPanel.setLayout(new BoxLayout(friendLabelPanel,BoxLayout.Y_AXIS));
        JCheckBox[] checkBoxes = new JCheckBox[friends.size()];
        Iterator<String> iterator = friends.iterator();
        int i = 0;
        while (iterator.hasNext()){
            String labelName = iterator.next();
            checkBoxes[i] = new JCheckBox(labelName);
            friendLabelPanel.add(checkBoxes[i]);
            i++;
        }
        //刷新
        friendLabelPanel.revalidate();
        //点击提交按钮，提交信息到服务器
        conformBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //判断哪些好友选中加入群聊，遍历判断
                Set<String> selectedFriends = new HashSet<>();
                Component[] components = friendLabelPanel.getComponents();
                for(Component component : components){
                    JCheckBox checkBox = (JCheckBox) component;
                    //表示被选中，加入集合中
                    if(checkBox.isSelected()){
                        String labelName = checkBox.getText();
                        selectedFriends.add(labelName);
                    }
                }
                selectedFriends.add(myName);
                // 获取群名输入框输入的群名称，
                String groupName = groupNameText.getText();
                // 将群名+选中的好友信息发送到服务器，
                //type 3, content groupName, to [user1, user2, ...]
                MessageVO messageVO = new MessageVO();
                messageVO.setType("3");
                messageVO.setContent(groupName);
                messageVO.setTo(CommUtils.object2Json(selectedFriends));
                try {
                    PrintStream out = new PrintStream(connect2Server.getOut(),true,"UTF-8");
                    out.println(CommUtils.object2Json(messageVO));
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                //将当前创建群的界面隐藏，刷新好友列表界面的群列表
                frame.setVisible(false);
                //添加群信息，并刷新
                friendsList.addGroup(groupName,selectedFriends);
                friendsList.loadGroupList();
            }
        });
    }
}
