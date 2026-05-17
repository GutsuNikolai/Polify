package org.example.polify.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import org.example.polify.user.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final Algorithm algorithm;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.getSecret());
    }

    public String issueAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getTtlSeconds());

        return JWT.create()
            .withIssuer(properties.getIssuer())
            .withSubject(String.valueOf(user.getId()))
            .withClaim("login", user.getLogin())
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm);
    }
}

