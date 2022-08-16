package com.example.demo.controller;

import com.example.demo.service.SocketServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/socketServer/{userId}")
@Component
@Slf4j
public class SocketServerController {
    private  static SocketServerService socketServerService;

    @Autowired
    public void setSocketServerService(SocketServerService serverService){
        socketServerService = serverService;
    }
    @OnOpen
    public void open(Session session, @PathParam(value = "userId") String userName) {
        socketServerService.doOpen(session, userName);
    }

    /**
     * 接受信息并解析转发
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        socketServerService.doMessage(message);
    }

    /**
     * 连接关闭触发，通过sessionId来移除
     * socketServers中客户端连接信息
     */
    @OnClose
    public void onClose(Session session) {
        socketServerService.doClose(session);
    }

    /**
     * 发生错误时触发
     *
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        socketServerService.doError(error, session);
    }
}
