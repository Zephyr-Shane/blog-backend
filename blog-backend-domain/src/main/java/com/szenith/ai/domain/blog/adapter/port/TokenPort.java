package com.szenith.ai.domain.blog.adapter.port;

/** Token 生成与校验端口（领域不依赖具体 JWT 实现） */
public interface TokenPort {
    String issue(String email, String name, String role);
    boolean validate(String token);
    String subject(String token);
}
