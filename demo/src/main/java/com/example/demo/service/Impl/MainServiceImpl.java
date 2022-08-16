package com.example.demo.service.Impl;

import com.example.demo.common.Message;
import com.example.demo.event.MessageEvent;
import com.example.demo.common.SnowflakeIdWorker;
import com.example.demo.service.MainService;
import com.example.demo.service.SocketServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final ConcurrentHashMap<Integer, Map.Entry<CountDownLatch, Message>> requestMap = new ConcurrentHashMap<>();
    private SocketServerService socketServerService;
    private static SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    public void setSnowflakeIdWorker(SnowflakeIdWorker worker){
        snowflakeIdWorker = worker;
    }
    @Autowired
    public void setSocketServerService(SocketServerService serverService){
        socketServerService = serverService;
    }
    @Override
    public Message sendSyncMessage(Message message) {
        String source = message.getSource();
        String target = message.getTarget();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        int id = snowflakeIdWorker.nextId();
        message.setId(id);
        requestMap.put(id, new AbstractMap.SimpleEntry<>(countDownLatch, null));
        socketServerService.sendMessage(message);
        boolean await = false;
        try {
            await = countDownLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message value = requestMap.get(id).getValue();
        requestMap.remove(id);
        return await ? value : null;
    }

    @Override
    public void sendAsyncMessage(Message message) {
        socketServerService.sendMessage(message);
    }

    @Override
    public List<String> getUsersList() {
        return socketServerService.getOnlineUsers();
    }

    @EventListener({MessageEvent.class})
    public void onEvent(MessageEvent event) {
        Message message = event.getMessage();
        String source = message.getSource();
        String target = message.getTarget();
        Integer id = message.getId();
        Map.Entry<CountDownLatch, Message> countDownLatchMessageEntry = requestMap.get(id);
        if(countDownLatchMessageEntry!=null){
            log.info("同步回复：{}", message);
            countDownLatchMessageEntry.setValue(message);
            countDownLatchMessageEntry.getKey().countDown();
        }
    }
}
