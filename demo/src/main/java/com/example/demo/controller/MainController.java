package com.example.demo.controller;

import com.example.demo.common.R;
import com.example.demo.common.Message;
import com.example.demo.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/index")
public class MainController {
    @Autowired
    private MainService mainService;
    @PostMapping("/sendSync")
    public R sendSyncMessage(@RequestBody Message message) throws InterruptedException {
        Message resultMsg = mainService.sendSyncMessage(message);
        return resultMsg!=null ? R.success(resultMsg) : R.error("timeout");
    }

    @PostMapping("/sendAsync")
    public R<String> sendAsyncMessage(@RequestBody Message message) {
        mainService.sendAsyncMessage(message);
        return R.success("success");
    }

    @GetMapping("/users")
    public R<List<String>> getUsersList() {
        List<String> usersList = mainService.getUsersList();
        return R.success(usersList);
    }

}
