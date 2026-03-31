package com.modelhub.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.auth.LoginRequest;
import com.modelhub.backend.dto.auth.LoginResponse;
import com.modelhub.backend.dto.auth.RegisterRequest;
import com.modelhub.backend.entity.SysLoginLog;
import com.modelhub.backend.entity.SysUser;
import com.modelhub.backend.mapper.SysLoginLogMapper;
import com.modelhub.backend.mapper.SysUserMapper;
import com.modelhub.backend.security.JwtTokenProvider;
import com.modelhub.backend.service.AuthService;
import com.modelhub.backend.service.TurnstileService;
import com.modelhub.backend.service.admin.AccessControlService;
import com.modelhub.backend.util.RequestIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private static final int FAIL_THRESHOLD = 5;
    private static final Duration FAIL_WINDOW = Duration.ofMinutes(10);
    private static final Duration LOCK_TIME = Duration.ofMinutes(10);

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TurnstileService turnstileService;
    private final AccessControlService accessControlService;
    private final SysLoginLogMapper loginLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HttpServletRequest request;

    public AuthServiceImpl(
            SysUserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            TurnstileService turnstileService,
            AccessControlService accessControlService,
            SysLoginLogMapper loginLogMapper,
            RedisTemplate<String, Object> redisTemplate,
            HttpServletRequest request
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.turnstileService = turnstileService;
        this.accessControlService = accessControlService;
        this.loginLogMapper = loginLogMapper;
        this.redisTemplate = redisTemplate;
        this.request = request;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String ip = RequestIpUtil.getClientIp(this.request);
        turnstileService.verify(request.getCaptchaToken(), ip);

        SysUser existing = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (existing != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "username already exists");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setOrgId(1L);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String ip = RequestIpUtil.getClientIp(this.request);
        checkLocked(ip, request.getUsername());
        turnstileService.verify(request.getCaptchaToken(), ip);

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordFailure(ip, request.getUsername());
            saveLoginLog(null, request.getUsername(), ip, 0, "invalid username or password");
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "invalid username or password");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            saveLoginLog(user.getId(), request.getUsername(), ip, 0, "user disabled");
            throw new BusinessException(HttpStatus.FORBIDDEN, "user disabled");
        }

        clearFailure(ip, request.getUsername());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        Set<String> roleCodes = accessControlService.listUserRoleCodes(user.getId());
        Set<String> permissions = accessControlService.listUserApiPermCodes(user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        long expiresAt = System.currentTimeMillis() + jwtTokenProvider.getExpirationSeconds() * 1000;
        LoginResponse response = new LoginResponse(token, expiresAt, user.getId(), user.getUsername());
        response.setOrgId(user.getOrgId());
        response.setRoles(new ArrayList<>(roleCodes));
        response.setPermissions(new ArrayList<>(permissions));
        saveLoginLog(user.getId(), request.getUsername(), ip, 1, "login success");
        return response;
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

    private void saveLoginLog(Long userId, String username, String ip, int status, String message) {
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setIp(ip);
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setStatus(status);
        log.setMessage(message);
        log.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
}
