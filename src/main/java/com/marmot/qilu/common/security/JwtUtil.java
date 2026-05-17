package com.marmot.qilu.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_IDENTITY_TYPE = "identityType";

    private static final String IDENTITY_USER = "USER";
    private static final String IDENTITY_ADMIN = "ADMIN";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateUserToken(String uuid, String email) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);
        return Jwts.builder()
                .subject(uuid)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_IDENTITY_TYPE, IDENTITY_USER)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateAdminToken(String uuid) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);
        return Jwts.builder()
                .subject(uuid)
                .claim(CLAIM_IDENTITY_TYPE, IDENTITY_ADMIN)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getIdentityType(Claims claims) {
        Object identityType = claims.get(CLAIM_IDENTITY_TYPE);
        return identityType == null ? null : identityType.toString();
    }

    public String getUuid(Claims claims) {
        Object uuid = claims.getSubject();
        return uuid == null ? null : uuid.toString();
    }

    public boolean isUserToken(Claims claims) {
        return IDENTITY_USER.equals(getIdentityType(claims));
    }

    public boolean isAdminToken(Claims claims) {
        return IDENTITY_ADMIN.equals(getIdentityType(claims));
    }

    public boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
