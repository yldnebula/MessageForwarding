package com.example.demo.service.Impl;

import com.example.demo.common.Message;
import com.example.demo.Event.MessageEvent;
import com.example.demo.controller.SocketServerController;
import com.example.demo.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MainServiceImpl implements MainService {
//    private CountDownLatch countDownLatch;
//    private Message send;
//    private Message recv;
    private static final ConcurrentHashMap<Map.Entry<String, String>, Map.Entry<CountDownLatch, Message>> requestMap = new ConcurrentHashMap<>();
    @Override
    public Message sendSyncMessage(Message message) {
        String source = message.getSource();
        String target = message.getTarget();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AbstractMap.SimpleEntry<String, String> stringStringSimpleEntry = new AbstractMap.SimpleEntry<>(source, target);
        requestMap.put(stringStringSimpleEntry, new AbstractMap.SimpleEntry<>(countDownLatch, null));
        SocketServerController.sendMessage(message);
        new Thread(() -> {
            try {
                Thread.sleep(31000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        boolean await = false;
        try {
            await = countDownLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message value = requestMap.get(stringStringSimpleEntry).getValue();
        return await ? value : null;
    }

    @Override
    public void sendAsyncMessage(Message message) {
        SocketServerController.sendMessage(message);
    }

    @Override
    public List<String> getUsersList() {
        return SocketServerController.getOnlineUsers();
    }

    @EventListener({MessageEvent.class})
    public void onEvent(MessageEvent event) {
        Message message = event.getMessage();
        String source = message.getSource();
        String target = message.getTarget();
        AbstractMap.SimpleEntry<String, String> stringStringSimpleEntry = new AbstractMap.SimpleEntry<>(target, source);
        Map.Entry<CountDownLatch, Message> countDownLatchMessageEntry = requestMap.get(stringStringSimpleEntry);
        if(countDownLatchMessageEntry!=null){
            log.info("同步回复：{}", message);
            countDownLatchMessageEntry.setValue(message);
            countDownLatchMessageEntry.getKey().countDown();
        }
    }
}
