package com.modelhub.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.auth.AccountSecurityProfileResponse;
import com.modelhub.backend.dto.auth.CancelAccountRequest;
import com.modelhub.backend.dto.auth.ChangePasswordRequest;
import com.modelhub.backend.dto.auth.ForgotPasswordCodeRequest;
import com.modelhub.backend.dto.auth.ForgotPasswordCodeResponse;
import com.modelhub.backend.dto.auth.ForgotPasswordResetRequest;
import com.modelhub.backend.dto.auth.LoginRequest;
import com.modelhub.backend.dto.auth.LoginResponse;
import com.modelhub.backend.dto.auth.RegisterRequest;
import com.modelhub.backend.dto.auth.SendContactVerifyCodeRequest;
import com.modelhub.backend.dto.auth.VerifyContactCodeRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private static final int FAIL_THRESHOLD = 5;
    private static final Duration FAIL_WINDOW = Duration.ofMinutes(10);
    private static final Duration LOCK_TIME = Duration.ofMinutes(10);
    private static final Duration RESET_CODE_TTL = Duration.ofMinutes(10);
    private static final Duration CONTACT_CODE_TTL = Duration.ofMinutes(10);
    private static final Duration CODE_COOLDOWN = Duration.ofSeconds(60);

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TurnstileService turnstileService;
    private final AccessControlService accessControlService;
    private final SysLoginLogMapper loginLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HttpServletRequest request;
    private final Random random = new Random();

    @Value("${app.auth.expose-verification-code:true}")
    private boolean exposeVerificationCode;

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

    @Override
    public ForgotPasswordCodeResponse sendForgotPasswordCode(ForgotPasswordCodeRequest request) {
        String username = request.getUsername().trim();
        String ip = RequestIpUtil.getClientIp(this.request);
        turnstileService.verify(request.getCaptchaToken(), ip);
        ensureCodeCooldown(forgotCodeCooldownKey(username), "please wait before requesting another reset code");

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "user not found or disabled");
        }

        String code = generateSixDigitCode();
        redisTemplate.opsForValue().set(forgotCodeKey(username), code, RESET_CODE_TTL);
        redisTemplate.opsForValue().set(forgotCodeCooldownKey(username), 1, CODE_COOLDOWN);
        return new ForgotPasswordCodeResponse(RESET_CODE_TTL.getSeconds(), exposeVerificationCode ? code : null);
    }

    @Override
    @Transactional
    public void resetPasswordByCode(ForgotPasswordResetRequest request) {
        String username = request.getUsername().trim();
        String ip = RequestIpUtil.getClientIp(this.request);
        turnstileService.verify(request.getCaptchaToken(), ip);

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "user not found or disabled");
        }

        Object cached = redisTemplate.opsForValue().get(forgotCodeKey(username));
        if (!(cached instanceof String) || !((String) cached).equals(request.getCode().trim())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid or expired reset code");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        redisTemplate.delete(forgotCodeKey(username));
        clearFailure(ip, username);
    }

    @Override
    public AccountSecurityProfileResponse getAccountSecurityProfile(Long userId) {
        SysUser user = requireActiveUser(userId);
        AccountSecurityProfileResponse response = new AccountSecurityProfileResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEmailVerified(user.getEmailVerified() == null ? 0 : user.getEmailVerified());
        response.setMobile(user.getMobile());
        response.setMobileVerified(user.getMobileVerified() == null ? 0 : user.getMobileVerified());
        return response;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = requireActiveUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public ForgotPasswordCodeResponse sendContactVerifyCode(Long userId, SendContactVerifyCodeRequest request) {
        SysUser user = requireActiveUser(userId);
        String ip = RequestIpUtil.getClientIp(this.request);
        turnstileService.verify(request.getCaptchaToken(), ip);

        String type = request.getType().trim().toUpperCase();
        String target = request.getTarget().trim();
        if (!StringUtils.hasText(target)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "target is required");
        }

        String cooldownKey = contactCodeCooldownKey(type, userId, target);
        ensureCodeCooldown(cooldownKey, "please wait before requesting another verification code");
        String code = generateSixDigitCode();
        redisTemplate.opsForValue().set(contactCodeKey(type, userId, target), code, CONTACT_CODE_TTL);
        redisTemplate.opsForValue().set(cooldownKey, 1, CODE_COOLDOWN);

        if ("EMAIL".equals(type)) {
            user.setEmail(target);
            user.setEmailVerified(0);
        } else if ("MOBILE".equals(type)) {
            user.setMobile(target);
            user.setMobileVerified(0);
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "unsupported verification type");
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return new ForgotPasswordCodeResponse(CONTACT_CODE_TTL.getSeconds(), exposeVerificationCode ? code : null);
    }

    @Override
    public void verifyContactCode(Long userId, VerifyContactCodeRequest request) {
        SysUser user = requireActiveUser(userId);
        String type = request.getType().trim().toUpperCase();
        String target = request.getTarget().trim();
        String code = request.getCode().trim();
        String key = contactCodeKey(type, userId, target);

        Object cached = redisTemplate.opsForValue().get(key);
        if (!(cached instanceof String) || !((String) cached).equals(code)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "invalid or expired verification code");
        }

        if ("EMAIL".equals(type)) {
            user.setEmail(target);
            user.setEmailVerified(1);
        } else if ("MOBILE".equals(type)) {
            user.setMobile(target);
            user.setMobileVerified(1);
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "unsupported verification type");
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        redisTemplate.delete(key);
    }

    @Override
    public void cancelAccount(Long userId, CancelAccountRequest request) {
        SysUser user = requireActiveUser(userId);
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "password is incorrect");
        }
        user.setStatus(0);
        user.setCancelTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        saveLoginLog(user.getId(), user.getUsername(), RequestIpUtil.getClientIp(this.request), 1, "account cancelled");
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

    private String forgotCodeKey(String username) {
        return "auth:reset:code:" + username;
    }

    private String forgotCodeCooldownKey(String username) {
        return "auth:reset:code:cooldown:" + username;
    }

    private String contactCodeKey(String type, Long userId, String target) {
        return "auth:verify:code:" + type + ":" + userId + ":" + target;
    }

    private String contactCodeCooldownKey(String type, Long userId, String target) {
        return "auth:verify:code:cooldown:" + type + ":" + userId + ":" + target;
    }

    private String generateSixDigitCode() {
        int value = random.nextInt(900000) + 100000;
        return String.valueOf(value);
    }

    private void ensureCodeCooldown(String cooldownKey, String message) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, message);
        }
    }

    private SysUser requireActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "user not found or disabled");
        }
        return user;
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
