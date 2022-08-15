package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.common.Message;
import com.example.demo.event.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/socketServer/{userId}")
@Component
@Slf4j
public class SocketServerController {
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static final String PING_STR = "0x9";
    private static final String PONG_STR = "0xA";
    private Session session;
    private static final String SYS_ID = "SERVER";
    private static final String ALL_ID = "ALL";
    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext appContext) {
        applicationContext= appContext;
    }
    @OnOpen
    public void open(Session session, @PathParam(value = "userId") String userName) {
        this.session = session;
        sessionMap.put(userName, session);
        log.info("客户端:【{}】连接成功", userName);
        //群发通知
        sendMessage(new Message<>(SYS_ID, ALL_ID, "客户端【"+userName+"】已上线！",3, getOnlineUsers()));
    }

    /**
     * 接受信息并解析转发
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        Message mess = JSON.parseObject(message, Message.class);
        String text = mess.getText();
        String source = mess.getSource();
        String target = mess.getTarget();

        if(text.equals(PING_STR)){
            //收到心跳包
            sendMessage(new Message<>(SYS_ID, source, PONG_STR, 1));
            return;
        }

        if(mess.getResponse()){
            applicationContext.publishEvent(new MessageEvent(this, mess));
        }else{
            log.info("客户端:【{}】发送信息:{} 给客户端【{}】", source, text, target);
            Session t_sess = sessionMap.get(target);
            if (t_sess == null) {
                log.info("发送信息失败，【{}】未上线", target);
            } else {
                sendMessage(mess);
            }
        }
    }

    /**
     * 连接关闭触发，通过sessionId来移除
     * socketServers中客户端连接信息
     */
    @OnClose
    public void onClose() {
        String username = null;
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                log.info("客户端:【{}】关闭连接", entry.getKey());
                username = entry.getKey();
                sessionMap.remove(entry.getKey());
            }
        }
        if(username!=null){
            sendMessage(new Message<>(SYS_ID, ALL_ID, "客户端【"+username+"】下线！", 4, getOnlineUsers()));
        }

    }

    /**
     * 发生错误时触发
     *
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        String username = null;
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                log.info("客户端:【{}】发生异常，下线", entry.getKey());
                username = entry.getKey();
                sessionMap.remove(entry.getKey());
                error.printStackTrace();
            }
        }
        if(username!=null){
            sendMessage(new Message<>(SYS_ID, ALL_ID, "客户端【" + username + "】下线！", 4, getOnlineUsers()));
        }
    }


    /**
     * 信息发送的方法，通过客户端的userName
     * 拿到其对应的session，调用信息推送的方法
     *
     * @param message
     */
    public synchronized static void sendMessage(Message message) {
        String target = message.getTarget();
        if(target.equals(ALL_ID)){
            for (Session session : sessionMap.values()) {
                if(session.isOpen()){
                    session.getAsyncRemote().sendText(JSON.toJSONString(message));
                }
            }
            log.info("推送给所有客户端 :【{}】", message.getText());
        }else{
            Session targetSession = sessionMap.get(target);
            if(targetSession.isOpen()){
                targetSession.getAsyncRemote().sendText(JSON.toJSONString(message));
                log.info("消息发送成功！");
            }else{
                log.info("客户端【{}】连接已关闭",target);
            }
        }
    }

    /**
     * 获取在线用户名，前端界面需要用到
     *
     * @return
     */
    public synchronized static List<String> getOnlineUsers() {
        List<String> onlineUsers = new ArrayList<>(sessionMap.keySet());
        return onlineUsers;
    }
}
