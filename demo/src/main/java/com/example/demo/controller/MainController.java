package com.example.demo.controller;

import com.example.demo.common.R;
import com.example.demo.entity.Message;
import com.example.demo.entity.MyEvent;
import com.example.demo.server.SocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/index")
public class MainController {

    @Autowired
    private SocketServer socketServer;
    @Autowired
    private ApplicationContext applicationContext;


    @PostMapping("/sendSync")
    public R<String> sendSyncMessage(@RequestBody Message message, HttpSession session) throws InterruptedException {
        log.info("同步Message:{};  {}", message.toString(), session);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @EventListener(classes = {MyEvent.class}) //classes属性指定处理事件的类型
            public void onEvent(MyEvent event){
                log.info("事件触发");
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        thread.join();
        return R.success("success");

    }

    @PostMapping("/sendAsync")
    public R<String> sendAsyncMessage(@RequestBody Message message){
        log.info("Message:{}", message.toString());
//        SocketServer.se
        return R.success("");
    }
}
