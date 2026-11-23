package com.szenith.ai.domain.blog.service;

import com.szenith.ai.domain.blog.adapter.port.SupportRepository;
import com.szenith.ai.domain.blog.adapter.port.TokenPort;
import com.szenith.ai.domain.blog.model.entity.User;
import com.szenith.ai.types.enums.ResponseCode;
import com.szenith.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/** 认证领域服务 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SupportRepository support;
    private final TokenPort tokenPort;

    public Map<String, Object> login(String email, String password) {
        User user = support.findUserByEmail(email);
        if (user == null || password == null
                || !sha256(password).equalsIgnoreCase(user.getPasswordHash())) {
            throw new AppException(ResponseCode.UNAUTHORIZED, "账号或密码错误");
        }
        String token = tokenPort.issue(user.getEmail(), user.getName(), user.getRole());
        User safe = User.builder().id(user.getId()).email(user.getEmail())
                .name(user.getName()).role(user.getRole()).build();
        return Map.of("token", token, "user", safe);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
