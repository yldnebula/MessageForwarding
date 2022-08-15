package com.example.demo.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD) // 只能放在方法上面
@Retention(RetentionPolicy.RUNTIME) // 将被JVM保留,所以他们能在运行时被JVM或其他使用反射机制的代码所读取和使用.
public @interface LimitRequest {
    long time() default 5000; // 限制时间 单位：毫秒
    int count() default 1; // 允许请求的次数
}
