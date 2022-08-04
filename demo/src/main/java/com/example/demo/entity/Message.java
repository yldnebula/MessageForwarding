package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String source;
    private String target;
    private String text;

    private boolean isSync=false;

    private boolean isResponse=false;

    public Message(String source, String target, String text){
        this.source = source;
        this.target = target;
        this.text = text;
    }
}
