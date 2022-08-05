package com.example.demo.service.Impl;

import com.example.demo.common.Message;
import com.example.demo.Event.MessageEvent;
import com.example.demo.controller.SocketServerController;
import com.example.demo.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
@Slf4j
public class MainServiceImpl implements MainService {
    private CountDownLatch countDownLatch;
    private Message message;
    @Override
    public Message sendSyncMessage(Message message){
        countDownLatch = new CountDownLatch(1);
        final boolean[] isTimeOut = {false};
        SocketServerController.sendMessage(message);
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                isTimeOut[0] = true;
                countDownLatch.countDown();
                //TODO
                //如果刚好阻塞完成，这时候收到event。。还是当作超时处理吗？
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isTimeOut[0]?null:this.message;
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
        log.info("同步回复：{}", message);
        this.message = message;
        countDownLatch.countDown();
    }
}
