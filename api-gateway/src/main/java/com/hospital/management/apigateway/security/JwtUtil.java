package com.hospital.management.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    private static String SECRET_KEY_STRING = "7CB916ua56wKRz0UJuURnd2wSEg2C0S3";
    private static SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());


    public String extractEmail(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String extractRole(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role",String.class);
    }
}
