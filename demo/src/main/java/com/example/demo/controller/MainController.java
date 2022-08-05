package com.example.demo.controller;

import com.example.demo.common.R;
import com.example.demo.entity.Message;
import com.example.demo.entity.MyEvent;
import com.example.demo.server.SocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/index")
public class MainController {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private Message message;

    @PostMapping("/sendSync")
    public R<Message> sendSyncMessage(@RequestBody Message message) throws InterruptedException {
        final boolean[] isTimeOut = {false};
        countDownLatch = new CountDownLatch(1);
        SocketServer.sendMessage(message);
        new Thread(() -> {
            try {
                Thread.sleep(30000);
                isTimeOut[0] = true;
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        countDownLatch.await();
        return !isTimeOut[0] ? R.success(this.message) : R.error("timeout");

    }

    @PostMapping("/sendAsync")
    public R<String> sendAsyncMessage(@RequestBody Message message) {
        log.info("Message:{}", message.toString());
        SocketServer.sendMessage(message);
        return R.success("success");
    }

    @GetMapping("/getUsers")
    public R<List<String>> getUsersList() {
        List<String> onlineUsers = SocketServer.getOnlineUsers();
        return R.success(onlineUsers);
    }

    @EventListener({MyEvent.class})
    public void onEvent(MyEvent event) {
        Message message = event.getMessage();
        log.info("同步回复：{}", message);
        this.message = message;
        countDownLatch.countDown();
    }
}
