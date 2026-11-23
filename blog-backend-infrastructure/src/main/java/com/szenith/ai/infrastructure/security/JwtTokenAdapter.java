package com.szenith.ai.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.szenith.ai.domain.blog.adapter.port.TokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

/** JWT 令牌实现：领域 TokenPort 的适配器 */
@Component
public class JwtTokenAdapter implements TokenPort {

    private final Algorithm algorithm;
    private final long ttlMillis;
    private final JWTVerifier verifier;

    public JwtTokenAdapter(
            @Value("${blog.jwt.secret}") String secret,
            @Value("${blog.jwt.ttl-hours}") long ttlHours) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.ttlMillis = ttlHours * 3600_000L;
        this.verifier = JWT.require(algorithm).build();
    }

    @Override
    public String issue(String email, String name, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("name", name)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusMillis(ttlMillis)))
                .sign(algorithm);
    }

    @Override
    public boolean validate(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String subject(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
