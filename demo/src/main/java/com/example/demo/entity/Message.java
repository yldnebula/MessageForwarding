package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int type = 0;//0:普通消息，1:上下线通知
    private String source;
    private String target;
    private String text;

    private T data;

    private Boolean sync = false;

    private Boolean response=false;

    public Message(String source, String target, String text){
        this.source = source;
        this.target = target;
        this.text = text;
    }

    public Message(String source, String target, String text, int type, T data){
        this.source = source;
        this.target = target;
        this.text = text;
        this.data = data;
        this.type = type;
    }

}
