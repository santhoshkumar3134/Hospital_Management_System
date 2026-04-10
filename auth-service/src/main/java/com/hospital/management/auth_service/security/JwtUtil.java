package com.hospital.management.auth_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static String SECRET_KEY_STRING = "7CB916ua56wKRz0UJuURnd2wSEg2C0S3";
    private static SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    public String generateToken(UserDetails userDetails){
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 *5))
                .signWith(secretKey,Jwts.SIG.HS256)
                .compact();
    }
}
