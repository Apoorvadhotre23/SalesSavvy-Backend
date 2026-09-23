package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Jwt_Tokens;
import com.example.demo.entity.Users;
import com.example.demo.repository.JwtTokensRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtService {

    private final SecretKey key;
    private final JwtTokensRepository jwtRepo;

    public JwtService(@Value("${jwt.secret}") String secret, JwtTokensRepository jwtRepo) {

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtRepo = jwtRepo;
    }


    // Generate or return existing JWT
    public String generateToken(Users user) {

        // 1. Check whether this user already has a token
        Optional<Jwt_Tokens> existingToken = jwtRepo.findByUser(user);


        // 2. If token exists, check whether it is still valid
        if (existingToken.isPresent()) {

            Jwt_Tokens oldToken = existingToken.get();

            if (oldToken.getExpires_at().isAfter(LocalDateTime.now())) {

                // Token is still valid
                return oldToken.getToken();
            }
        }


        // 3. Token doesn't exist OR old token expired
        // Generate a new JWT

        long currentTime =
                System.currentTimeMillis();

        long expirationTime =
                currentTime + (60 * 60 *1000);


        String token = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("role", user.getRole().toString())
                .claim("username", user.getUsername())
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .signWith(key)
                .compact();


        // 4. Decide whether to UPDATE or INSERT

        Jwt_Tokens jwtToken;

        if (existingToken.isPresent()) {

            // Token row already exists.
            // Reuse it and update the token.
            jwtToken = existingToken.get();

        } else {

            // First token for this user.
            // Create new database row.
            jwtToken = new Jwt_Tokens();

            jwtToken.setUser(user);
        }


        // 5. Set new JWT
        jwtToken.setToken(token);


        // 6. Set new expiration time
        jwtToken.setExpires_at(
                LocalDateTime.now().plusHours(1)
        );


        // 7. Save
        jwtRepo.save(jwtToken);


        // 8. Return token
        return token;
    }


    // Add JWT to cookie
    public void addCookie(String token, HttpServletResponse response) {

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);

        // false for local host development
        cookie.setSecure(false);
        cookie.setPath("/");

        // 1 hour
        cookie.setMaxAge(60*60);

        response.addCookie(cookie);
    }

    
    public boolean validateToken(String token) {
    	
    	try {
    		
    		Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token);
    	 
    		return true;
    		
    	}catch (Exception e) {
			
    		return false;
    		
		}
    }
    
    public String extractUsername(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }
}