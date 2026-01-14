package io.github.tch12345.javaweb.service;
import io.github.tch12345.javaweb.repository.UserRepository;
import io.github.tch12345.javaweb.table.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expiration}")
    private long EXPIRATION_MS;

    private SecretKey key;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }


    public User getUserFromToken(String token) {
        if (token == null || token.isEmpty()) return null;
        if (!validateToken(token)) return null;
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date issuedAt = claims.getIssuedAt();
            Date expiration = claims.getExpiration();
            if (issuedAt != null && expiration != null) {
                long durationMillis = expiration.getTime() - issuedAt.getTime();
                if (durationMillis > EXPIRATION_MS) {
                    System.out.println("old Token expired");
                    return null;
                }
            }
            String email = claims.getSubject();
            return userRepository.findByEmail(email).orElse(null);
        }
        catch (JwtException e) {
            System.out.println("invalid token");
            return null;
        }
    }
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) return false;
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date issuedAt = claims.getIssuedAt();
            if (issuedAt == null) return false;

            long now = System.currentTimeMillis();
            if (now - issuedAt.getTime() > EXPIRATION_MS) {
                System.out.println("Token expired by new EXPIRATION_MS");
                return false;
            }
            return true;
        } catch (JwtException e) {
            System.out.println("Token invalid");
            return false;
        }
    }
}
