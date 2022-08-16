package com.example.demo.service.Impl;

import com.alibaba.fastjson.JSON;
import com.example.demo.common.Message;
import com.example.demo.event.MessageEvent;
import com.example.demo.service.SocketServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SocketServerServiceImpl implements SocketServerService {
    private static final String PING_STR = "0x9";
    private static final String PONG_STR = "0xA";
    private static final String SYS_ID = "SERVER";
    private static final String ALL_ID = "ALL";
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext appContext) {
        applicationContext= appContext;
    }

    @Override
    public void doOpen(Session session, String userName) {
        sessionMap.put(userName, session);
        log.info("客户端:【{}】连接成功", userName);
        sendMessage(new Message<>(SYS_ID, ALL_ID, "客户端【"+userName+"】已上线！",3, getOnlineUsers()));
    }

    @Override
    public void doClose(Session session) {
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

    @Override
    public void doError(Throwable error, Session session) {
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

    @Override
    public void doMessage(String message) {
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

    @Override
    public void sendMessage(Message message) {
        String target = message.getTarget();
        if(target.equals(ALL_ID)){
            for (Session session : sessionMap.values()) {
                synchronized (session){
                    if(session.isOpen()){
                        session.getAsyncRemote().sendText(JSON.toJSONString(message));
                    }
                }
            }
            log.info("推送给所有客户端 :【{}】", message.getText());
        }else{
            Session targetSession = sessionMap.get(target);
            synchronized (targetSession){
                if(targetSession.isOpen()){
                    targetSession.getAsyncRemote().sendText(JSON.toJSONString(message));
                    log.info("消息发送成功！");
                }else{
                    log.info("客户端【{}】连接已关闭",target);
                }
            }
        }
    }

    @Override
    public List<String> getOnlineUsers() {
        return new ArrayList<>(sessionMap.keySet());
    }
}
