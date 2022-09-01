package com.example.demo.service.Impl;

import com.alibaba.fastjson.JSON;
import com.example.demo.common.Dict;
import com.example.demo.common.HeartBeatMap;
import com.example.demo.common.Message;
import com.example.demo.event.MessageEvent;
import com.example.demo.service.SocketServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@EnableScheduling
@Slf4j
public class SocketServerServiceImpl implements SocketServerService {

    private final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, HeartBeatMap> timeCountMap = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext appContext) {
        applicationContext = appContext;
    }

    @Override
    public void doOpen(Session session, String userName) {
        sessionMap.put(userName, session);
        timeCountMap.put(userName, new HeartBeatMap(LocalDateTime.now(), 0));
        log.info("客户端:【{}】连接成功", userName);
        sendMessage(new Message<>(Dict.SYS_ID, Dict.ALL_ID, "客户端【" + userName + "】已上线！", 3, getOnlineUsers()));
    }

    @Override
    public void doClose(Session session) {
        String username = null;
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                log.info("客户端:【{}】关闭连接", entry.getKey());
                username = entry.getKey();
                sessionMap.remove(entry.getKey());
                timeCountMap.remove(username);
            }
        }
        if (username != null) {
            sendMessage(new Message<>(Dict.SYS_ID, Dict.ALL_ID, "客户端【" + username + "】下线！", 4, getOnlineUsers()));
        }
    }

    @Override
    public void doError(Throwable error, Session session) {
        log.info("Error");
        error.printStackTrace();
    }

    @Override
    public void doMessage(String message) {
        Message mess = JSON.parseObject(message, Message.class);
        String text = mess.getText();
        String source = mess.getSource();
        String target = mess.getTarget();
        if(mess.getMock()){
            //测试消息
            log.info("mock");
            sendMessage(mess);
            return;
        }

        if (text.equals(Dict.PING_STR)) {
            //收到心跳包
            handleHeartBeat(source);
            return;
        }

        if (mess.getResponse()) {
            applicationContext.publishEvent(new MessageEvent(this, mess));
        } else {
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
        if (target.equals(Dict.ALL_ID)) {
            for (Session session : sessionMap.values()) {
                    if (session.isOpen()) {
                        try{
                            session.getBasicRemote().sendText(JSON.toJSONString(message));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
            }
            log.info("推送给所有客户端 :【{}】  {}", message.getText(), sessionMap.size());
        } else {
            Session targetSession = sessionMap.get(target);
            if(targetSession == null){
                log.info("发送信息失败，【{}】未上线", target);
                return;
            }
                if (targetSession.isOpen()) {
                    try{
                        targetSession.getBasicRemote().sendText(JSON.toJSONString(message));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    log.info("消息发送成功！");
                } else {
                    log.info("客户端【{}】连接已关闭", target);
                }
        }
    }

    @Override
    public synchronized void sendMessageLock(Message message) {
        log.info("img->synchronized");
        String target = message.getTarget();
        if (target.equals(Dict.ALL_ID)) {
            for (Session session : sessionMap.values()) {
                if (session.isOpen()) {
                    try{
                        session.getBasicRemote().sendText(JSON.toJSONString(message));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            log.info("推送给所有客户端 :【{}】  {}", message.getText(), sessionMap.size());
        } else {
            Session targetSession = sessionMap.get(target);
            if(targetSession == null){
                log.info("发送信息失败，【{}】未上线", target);
                return;
            }
            if (targetSession.isOpen()) {
                try{
                    targetSession.getBasicRemote().sendText(JSON.toJSONString(message));
                }catch (Exception e){
                    e.printStackTrace();
                }
                log.info("消息发送成功！");
            } else {
                log.info("客户端【{}】连接已关闭", target);
            }
        }
    }


    @Override
    public List<String> getOnlineUsers() {
        return new ArrayList<>(sessionMap.keySet());
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void heartBeatCheck(){
        if(timeCountMap.size() == 0){
            return;
        }
        log.info("heartbeat check: {}, {}", LocalDateTime.now(), Thread.currentThread().getId());
        for (String name: timeCountMap.keySet()){
            HeartBeatMap heartBeatMap = timeCountMap.get(name);
            if(LocalDateTime.now().getSecond() -heartBeatMap.getLastUpdateTime().getSecond()> 5){
                if(heartBeatMap.getTimeOutCount().get() > 3){
                    doClose(sessionMap.get(name));
                }else{
                    heartBeatMap.getTimeOutCount().getAndIncrement();
                }
            }
        }
    }

    @Async("heartBeatExecutor")
    public void handleHeartBeat(String userId){
        sendMessage(new Message<>(Dict.SYS_ID, userId, Dict.PONG_STR, 1));
        HeartBeatMap heartBeatMap = timeCountMap.get(userId);
        heartBeatMap.setLastUpdateTime(LocalDateTime.now());
        log.info(heartBeatMap.toString());
    }
}
