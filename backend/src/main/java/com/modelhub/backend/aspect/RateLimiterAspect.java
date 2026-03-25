package com.modelhub.backend.aspect;

import com.modelhub.backend.annotation.RateLimiter;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.util.RequestIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
public class RateLimiterAspect {
    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimiterAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimiter)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = RequestIpUtil.getClientIp(request);
        String key = rateLimiter.keyPrefix() + ":" + ip;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(rateLimiter.windowSeconds()));
        }
        if (count != null && count > rateLimiter.maxRequests()) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "request too frequent");
        }

        return joinPoint.proceed();
    }
}

