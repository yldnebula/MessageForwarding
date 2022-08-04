package com.example.demo.entity;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class MyEvent extends ApplicationEvent {
    @Getter
    private Message message;
    /**
     * 构造器
     *
     * @param source
     *            该事件的相关数据
     *
     * @date 2019/11/19 6:40
     */
    public MyEvent(Object source, Message message) {
        super(source);
        this.message = message;
    }
}