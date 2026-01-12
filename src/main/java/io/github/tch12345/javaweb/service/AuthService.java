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

    public boolean isLoggedIn(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token Expired");
        } catch (JwtException e) {
            System.out.println("Token Invalid");
        }
        return false;
    }

    public User getUserFromToken(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            return userRepository.findByEmail(email).orElse(null);
        } catch (JwtException e) {
            System.out.println("无效 token");
            return null;
        }
    }
}
