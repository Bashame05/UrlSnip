package com.makar.UrlSnip.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private final long issueTime = System.currentTimeMillis();
    private final long expirationDuration = Duration.ofHours(10).toMillis();
    private final SecretKey secretKey;
    public JwtService(@Value("${JWT_SECRET}") String secret){
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String userName){
        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date(issueTime))
                .expiration(new Date(issueTime + expirationDuration))
                .signWith(secretKey)
                .compact();
    }

    public Claims extractPayloadFromToken(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(Function<Claims , T> claimsResolver, String token){
        return claimsResolver.apply(extractPayloadFromToken(token));
    }

    public String extractUsername(String token){
        return extractClaim(Claims::getSubject,token);
    }

    public Date extractExpiration(String token){
        return extractClaim(Claims::getExpiration,token);
    }

    public boolean isTokenNotExpired(String token){
        return extractExpiration(token).after(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails){
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && isTokenNotExpired(token));
    }
}
