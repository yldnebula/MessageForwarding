package com.example.demo.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
public class HeartBeatMap {
    private LocalDateTime lastUpdateTime;
    private AtomicInteger timeOutCount;

    public HeartBeatMap(LocalDateTime time, int count){
        this.lastUpdateTime = time;
        this.timeOutCount = new AtomicInteger(count);
    }
}
