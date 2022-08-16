package com.example.demo.service;

import com.example.demo.common.Message;
import javax.websocket.Session;
import java.util.List;


public interface SocketServerService {
    void doOpen(Session session, String userName);
    void doClose(Session session);
    void doError(Throwable error, Session session);
    void doMessage(String message);
    void sendMessage(Message message);
    List<String> getOnlineUsers();

}
