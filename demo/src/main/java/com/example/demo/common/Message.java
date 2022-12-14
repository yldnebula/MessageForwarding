package com.example.demo.common;

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
    private Integer id;
    private Integer type = 0;//0:普通消息，1:心跳包, 2:错误消息, 3:上线通知, 4:下线通知, 5.图片消息
    private String source;
    private String target;
    private String text;
    private T data;

    private Boolean sync = false;

    private Boolean response=false;

    private Boolean mock = false;//是否测试消息

    public Message(String source, String target, String text){
        this.source = source;
        this.target = target;
        this.text = text;
    }
    public Message(String source, String target, String text, int type){
        this.source = source;
        this.target = target;
        this.text = text;
        this.data = data;
        this.type = type;
    }
    public Message(String source, String target, String text, int type, T data){
        this.source = source;
        this.target = target;
        this.text = text;
        this.data = data;
        this.type = type;
    }

    public static <T> Message<T> error(String msg) {
        Message r = new Message();
        r.text = msg;
        r.type = 2;
        return r;
    }
}
