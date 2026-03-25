package com.modelhub.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.auth.LoginRequest;
import com.modelhub.backend.dto.auth.LoginResponse;
import com.modelhub.backend.dto.auth.RegisterRequest;
import com.modelhub.backend.entity.SysUser;
import com.modelhub.backend.mapper.SysUserMapper;
import com.modelhub.backend.security.JwtTokenProvider;
import com.modelhub.backend.service.AuthService;
import com.modelhub.backend.util.RequestIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private static final int FAIL_THRESHOLD = 5;
    private static final Duration FAIL_WINDOW = Duration.ofMinutes(10);
    private static final Duration LOCK_TIME = Duration.ofMinutes(10);

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HttpServletRequest request;

    public AuthServiceImpl(
            SysUserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RedisTemplate<String, Object> redisTemplate,
            HttpServletRequest request
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.request = request;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        SysUser existing = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (existing != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "username already exists");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String ip = RequestIpUtil.getClientIp(this.request);
        checkLocked(ip, request.getUsername());

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordFailure(ip, request.getUsername());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "invalid username or password");
        }

        clearFailure(ip, request.getUsername());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        long expiresAt = System.currentTimeMillis() + jwtTokenProvider.getExpirationSeconds() * 1000;
        return new LoginResponse(token, expiresAt, user.getId(), user.getUsername());
    }

    private void checkLocked(String ip, String username) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockIpKey(ip)))
                || Boolean.TRUE.equals(redisTemplate.hasKey(lockUserKey(username)))) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "account temporarily locked, try later");
        }
    }

    private void recordFailure(String ip, String username) {
        Long ipFailCount = redisTemplate.opsForValue().increment(failIpKey(ip));
        if (ipFailCount != null && ipFailCount == 1L) {
            redisTemplate.expire(failIpKey(ip), FAIL_WINDOW);
        }

        Long userFailCount = redisTemplate.opsForValue().increment(failUserKey(username));
        if (userFailCount != null && userFailCount == 1L) {
            redisTemplate.expire(failUserKey(username), FAIL_WINDOW);
        }

        if (ipFailCount != null && ipFailCount >= FAIL_THRESHOLD) {
            redisTemplate.opsForValue().set(lockIpKey(ip), 1, LOCK_TIME);
        }
        if (userFailCount != null && userFailCount >= FAIL_THRESHOLD) {
            redisTemplate.opsForValue().set(lockUserKey(username), 1, LOCK_TIME);
        }
    }

    private void clearFailure(String ip, String username) {
        redisTemplate.delete(failIpKey(ip));
        redisTemplate.delete(failUserKey(username));
    }

    private String failIpKey(String ip) {
        return "auth:fail:ip:" + ip;
    }

    private String failUserKey(String username) {
        return "auth:fail:user:" + username;
    }

    private String lockIpKey(String ip) {
        return "auth:lock:ip:" + ip;
    }

    private String lockUserKey(String username) {
        return "auth:lock:user:" + username;
    }
}

