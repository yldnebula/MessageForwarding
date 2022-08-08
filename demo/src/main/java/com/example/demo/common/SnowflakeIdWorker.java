package com.example.demo.common;


import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdWorker {
    /** 开始时间戳 (2019-01-01) */
    private final int twepoch = 25771200;// 1546272000000L/1000/60;

    /** 序列在id中占的位数 */
    private final long sequenceBits = 7L;

    /** 时间截向左移7位 */
    private final long timestampLeftShift = sequenceBits;

    /** 生成序列的掩码，这里为127 */
    private final int sequenceMask = -1 ^ (-1 << sequenceBits);

    /** 分钟内序列(0~127) */
    private int sequence = 0;
    private int laterSequence = 0;

    /** 上次生成ID的时间戳 */
    private int lastTimestamp = -1;

    private final MinuteCounter counter = new MinuteCounter();

    /** 预支时间标志位 */
    boolean isAdvance = false;

    // ==============================Constructors=====================================
    public SnowflakeIdWorker() {

    }

    // ==============================Methods==========================================
    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized int nextId() {


        int timestamp = timeGen();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if(timestamp > counter.get()) {
            counter.set(timestamp);
            isAdvance = false;
        }

        // 如果是同一时间生成的，则进行分钟内序列
        if (lastTimestamp == timestamp || isAdvance) {
            if(!isAdvance) {
                sequence = (sequence + 1) & sequenceMask;
            }

            // 分钟内自增列溢出
            if (sequence == 0) {
                // 预支下一个分钟,获得新的时间戳
                isAdvance = true;
                int laterTimestamp = counter.get();
                if (laterSequence == 0) {
                    laterTimestamp = counter.incrementAndGet();
                }

                int nextId = ((laterTimestamp - twepoch) << timestampLeftShift) //
                        | laterSequence;
                laterSequence = (laterSequence + 1) & sequenceMask;
                return nextId;
            }
        }
        // 时间戳改变，分钟内序列重置
        else {
            sequence = 0;
            laterSequence = 0;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成32位的ID
        return ((timestamp - twepoch) << timestampLeftShift) //
                | sequence;
    }

    /**
     * 返回以分钟为单位的当前时间
     *
     * @return 当前时间(分钟)
     */
    protected int timeGen() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000 / 60);
        return Integer.parseInt(timestamp);
    }
}