package com.example.demo.server;

import com.alibaba.fastjson.JSON;
import com.example.demo.entity.Message;
import com.example.demo.entity.MyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
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
public class SocketServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    private Session session;

    @Autowired
    private ApplicationContext applicationContext;


    @OnOpen
    public void open(Session session, @PathParam(value = "userId") String userName) {
        this.session = session;
//			if(sessionMap.containsKey(userName)){
//				logger.info("客户端:【{}】连接成功",userName);
//
//			}else{
//
//			}
        sessionMap.put(userName, session);
        logger.info("客户端:【{}】连接成功", userName);
        //群发通知
        sendAll(new Message("server", "all", "客户端【"+userName+"】已上线！"));
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

        applicationContext.publishEvent(new MyEvent(this));

        logger.info("客户端:【{}】发送信息:{} 给客户端【{}】", source, text, target);
        Session t_sess = sessionMap.get(target);
        if (t_sess == null) {
            logger.info("发送信息失败，【{}】未上线", target);
        } else {
            sendMessage(mess, t_sess);
        }
    }

    /**
     * 连接关闭触发，通过sessionId来移除
     * socketServers中客户端连接信息
     */
    @OnClose
    public void onClose() {
        String username = "";
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                logger.info("客户端:【{}】关闭连接", entry.getKey());
                username = entry.getKey();
                sessionMap.remove(entry.getKey());
            }
        }
        //群发通知
        sendAll(new Message("server", "all", "客户端【"+username+"】下线！"));
    }

    /**
     * 发生错误时触发
     *
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        String username = "";
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                logger.info("客户端:【{}】发生异常，下线", entry.getKey());
                username = entry.getKey();
                sessionMap.remove(entry.getKey());
                error.printStackTrace();
            }
        }
        //群发通知
        sendAll(new Message("server", "all", "客户端【"+username+"】下线！"));
    }


    /**
     * 信息发送的方法，通过客户端的userName
     * 拿到其对应的session，调用信息推送的方法
     *
     * @param message
     * @param targetSession
     */
    public synchronized static void sendMessage(Message message, Session targetSession) {
        targetSession.getAsyncRemote().sendText(JSON.toJSONString(message));
        logger.info("消息发送成功！");
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

    /**
     * 信息群发
     */
    public synchronized static void sendAll(Message message) {
        for (Session sess : sessionMap.values()) {
            sendMessage(message, sess);
        }

        logger.info("推送给所有客户端 :【{}】", message.getText());
    }
}
