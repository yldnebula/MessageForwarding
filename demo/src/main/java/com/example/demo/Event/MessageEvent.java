package com.example.demo.Event;

import com.example.demo.common.Message;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class MessageEvent extends ApplicationEvent {
    @Getter
    private Message message;

    /**
     * 消息事件
     * @param source
     * @param message
     */
    public MessageEvent(Object source, Message message) {
        super(source);
        this.message = message;
    }
}