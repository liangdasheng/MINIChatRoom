package com.lsj.java.client.service;

import com.lsj.java.util.CommUtils;
import com.lsj.java.vo.MessageVO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsList {
    private JPanel friendsPanel;
    private JScrollPane friendsList;
    private JButton createGroupbtn;
    private JScrollPane groupListPanel;
    private JFrame frame;

    private String userName;
    //存放在线好友信息
    private Set<String> users;
    //存储所有群名称以及群好友信息
    private Map<String,Set<String>> groupList = new ConcurrentHashMap<>();
    private Connect2Server connect2Server;
    //存放点击事件，私聊界面
    private Map<String,PrivateChatGUI> privateChatGUIList = new ConcurrentHashMap<>();
    //存放点击事件，群聊界面
    private Map<String,GroupChatGUI> groupChatGUIList = new ConcurrentHashMap<>();

    //好友列表的后台线程，不断地监听服务器发来的信息
    //信息包括：json字符串、或者普通字符串（Login等，表示注册或者登陆）
    //判断方法：判断开头是否为 { 括号
    //可以监听到：好友的上线信息、用户私聊、群聊
    private class DaemonTask implements Runnable{
        private Scanner scanner = new Scanner(connect2Server.getIn());
        @Override
        public void run() {
            while(true){
                if(scanner.hasNextLine()){
                    String strFromServer = scanner.nextLine();
                    if(strFromServer.startsWith("{")){
                        //json -> Object
                        MessageVO messageVO = (MessageVO) CommUtils.json2Object(strFromServer,MessageVO.class);
                        if(messageVO.getType().equals("2")){
                            //服务器发来的私聊信息
                            String friendName = messageVO.getContent().split("-")[0];
                            String msg = messageVO.getContent().split("-")[1];
                            //判断私聊是否是第一次创建
                            if(privateChatGUIList.containsKey(friendName)){
                                PrivateChatGUI privateChatGUI = privateChatGUIList.get(friendName);
                                privateChatGUI.getFrame().setVisible(true);
                                privateChatGUI.readFromServer(friendName + "说：" + msg);
                            }else{
                                PrivateChatGUI privateChatGUI = new PrivateChatGUI(friendName,userName,connect2Server);
                                privateChatGUIList.put(friendName,privateChatGUI);
                                privateChatGUI.readFromServer(friendName + "说：" + msg);
                            }
                        } else if(messageVO.getType().equals("4")){
                            //受到服务器发来的群聊信息,type 4, content sender-msg, to groupName-[1,2,,]
                            String groupName = messageVO.getTo().split("-")[0];
                            String senderName = messageVO.getContent().split("-")[0];
                            String groupMsg = messageVO.getContent().split("-")[1];
                            //若此群聊在群聊列表中，只需要弹出聊天界面（分两种：有就弹出，没有就new）
                            if(groupList.containsKey(groupName)){
                                if(groupChatGUIList.containsKey(groupName)){
                                    GroupChatGUI groupChatGUI = groupChatGUIList.get(groupName);
                                    groupChatGUI.getFrame().setVisible(true);
                                    groupChatGUI.readFromServer(senderName + "说：" + groupMsg);
                                }else{
                                    Set<String> names = groupList.get(groupName);
                                    GroupChatGUI groupChatGUI = new GroupChatGUI(groupName, names, userName, connect2Server);
                                    groupChatGUIList.put(groupName,groupChatGUI);
                                    groupChatGUI.readFromServer(senderName + "说：" + groupMsg);
                                }
                            }else{
                                //若群成员第一次收到群聊信息
                                //将群名称以及群成员保存到当前客户端群聊列表
                                Set<String> friends = (Set<String>) CommUtils.json2Object(messageVO.getTo().split("-")[1],Set.class);
                                groupList.put(groupName, friends);
                                loadGroupList();
                                //弹出群聊界面，缓存中一定没有，因为群名称都没有，所以直接new
                                GroupChatGUI groupChatGUI = new GroupChatGUI(groupName, friends, userName, connect2Server);
                                groupChatGUIList.put(groupName, groupChatGUI);
                                groupChatGUI.readFromServer(senderName + "说：" + groupMsg);
                            }
                        }
                    }else{
                        //服务器发来新用户上线提醒，newLogin:userName
                        if(strFromServer.startsWith("newLogin:")){
                            //按照 : 分开的第二个参数
                            String newFriendName = strFromServer.split(":")[1];
                            users.add(newFriendName);
                            //用弹框提醒用户上线
                            JOptionPane.showMessageDialog(frame, newFriendName + "上线了！",
                                    "上线提醒", JOptionPane.INFORMATION_MESSAGE);
                            //刷新好友列表
                            loadUsers();
                        }
                    }
                }
            }
        }
    }
    //点击鼠标触发私聊事件，标签点击事件
    private class PrivateLableAction implements MouseListener{
        private String labelName;
        public PrivateLableAction(String labelName){
            this.labelName = labelName;
        }
        //点击鼠标执行的事件，点击鼠标之后弹出私聊界面
        @Override
        public void mouseClicked(MouseEvent e) {
            //判断好友的私聊界面缓存是否已经有指定标签
            //如果有，说明是第二次访问，第一次已经关闭，所以只需要唤醒
            if(privateChatGUIList.containsKey(labelName)){
                PrivateChatGUI privateChatGUI = privateChatGUIList.get(labelName);
                privateChatGUI.getFrame().setVisible(true);
            }else{
                //否则是第一次点击，创建私聊界面
                PrivateChatGUI privateChatGUI = new PrivateChatGUI(labelName,userName,connect2Server);
                //加入到Map中
                privateChatGUIList.put(labelName,privateChatGUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
    //点击鼠标触发群聊事件，标签点击事件
    private class GroupLabelAction implements MouseListener{
        private String groupName;
        public GroupLabelAction(String groupName){
            this.groupName = groupName;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            //已经在缓存中，直接唤醒即可
            if(groupChatGUIList.containsKey(groupName)){
                GroupChatGUI groupChatGUI = groupChatGUIList.get(groupName);
                groupChatGUI.getFrame().setVisible(true);
            }else{
                //否则是第一次点击
                Set<String> names = groupList.get(groupName);
                GroupChatGUI groupChatGUI = new GroupChatGUI(groupName,names,userName,connect2Server);
                //加入到缓存中
                groupChatGUIList.put(groupName,groupChatGUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public FriendsList(String userName, Set<String> users, Connect2Server connect2Server){
        this.userName = userName;
        this.users = users;
        this.connect2Server = connect2Server;
        frame = new JFrame(userName);
        frame.setContentPane(friendsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        loadUsers();
        //启动后台线程监听服务器发来的消息
        Thread daemonThread = new Thread(new DaemonTask());
        daemonThread.setDaemon(true);
        daemonThread.start();

        //创建群组
        createGroupbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateGroupGUI(userName, users,connect2Server,FriendsList.this);
            }
        });
    }

    //加载所有在线用户信息，标签形式存放
    public void loadUsers(){
        JLabel[] userLabels = new JLabel[users.size()];
        JPanel friends = new JPanel();
        //盒子存放，垂直排列
        friends.setLayout(new BoxLayout(friends, BoxLayout.Y_AXIS));
        //set集合遍历
        Iterator<String> iterator = users.iterator();
        int i = 0;
        while(iterator.hasNext()){
            String userName = iterator.next();
            userLabels[i] = new JLabel(userName);
            //添加标签的点击事件，每个标签都添加
            userLabels[i].addMouseListener(new PrivateLableAction(userName));
            friends.add(userLabels[i]);
            i++;
        }
        //将friends这个Panel放在friendsList这个Panel中
        friendsList.setViewportView(friends);
        //滚动条设置为竖向滚动
        friendsList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //刷新列表
        friends.revalidate();
        friendsList.revalidate();
    }
    //更新群组
    public void loadGroupList(){
        //存储所有群组的名称标签
        JPanel groupNamePanel = new JPanel();
        groupNamePanel.setLayout(new BoxLayout(groupNamePanel,BoxLayout.Y_AXIS));
        JLabel[] labels = new JLabel[groupList.size()];
        //Map遍历
        Set<Map.Entry<String,Set<String>>> entries = groupList.entrySet();
        Iterator<Map.Entry<String,Set<String>>> iterator = entries.iterator();
        int i = 0;
        while (iterator.hasNext()){
            Map.Entry<String,Set<String>> entry = iterator.next();
            labels[i] = new JLabel(entry.getKey());
            //添加鼠标点击事件，唤起群聊界面
            labels[i].addMouseListener(new GroupLabelAction(entry.getKey()));
            groupNamePanel.add(labels[i]);
            i++;
        }
        groupListPanel.setViewportView(groupNamePanel);
        //设置纵向
        groupListPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //刷新
        groupListPanel.revalidate();
    }
    public void addGroup(String groupName, Set<String> friends){
        groupList.put(groupName,friends);
    }
}
