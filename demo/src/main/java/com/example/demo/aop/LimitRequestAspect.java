package com.example.demo.aop;

import com.example.demo.annotation.LimitRequest;
import com.example.demo.common.R;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
@Slf4j
public class LimitRequestAspect {
    private static ConcurrentHashMap<String, ExpiringMap<String, AtomicInteger>> limitMap = new ConcurrentHashMap<>();

    @Around("@annotation(limitRequest)")
    public Object doAround(ProceedingJoinPoint pjp, LimitRequest limitRequest) throws Throwable {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();

        String ip = request.getLocalAddr();
        String uri = request.getRequestURI();
        String key = "req_limit_".concat(ip);
        ExpiringMap<String, AtomicInteger> uriCountMap = limitMap.getOrDefault(key, ExpiringMap.builder().variableExpiration().build());
        AtomicInteger count = uriCountMap.getOrDefault(uri, new AtomicInteger(0));

        if(count.get() >= limitRequest.count()){
            log.info("用户IP[" + ip + "]访问地址[" + uri + "]超过了限定的次数[" + limitRequest.count() + "]");
            return R.error("请求次数过多，请稍后重试");
        }else if(count.get() == 0){
            //第一次来
            count.getAndIncrement();
            uriCountMap.put(uri, count, ExpirationPolicy.CREATED, limitRequest.time(), TimeUnit.MILLISECONDS);
        }else{
            count.getAndIncrement();
            uriCountMap.put(uri, count);
        }
        limitMap.put(key, uriCountMap);

        Object proceed = pjp.proceed();

        return proceed;
    }


}
