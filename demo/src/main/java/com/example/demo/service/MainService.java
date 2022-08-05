package com.example.demo.service;

import com.example.demo.common.Message;

import java.util.List;

public interface MainService {
    Message sendSyncMessage(Message message);
    void sendAsyncMessage(Message message);
    List<String> getUsersList();
}
