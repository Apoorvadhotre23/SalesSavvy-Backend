package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Users;

import io.jsonwebtoken.Jwts;

@Service
public class JwtService {
	
	 private final SecretKey key = Jwts.SIG.HS256.key().build();
	 
	 public String generateToken(Users user) {

	        long currentTime = System.currentTimeMillis();

	        long expirationTime = currentTime + 60 * 60 * 1000;

	        return Jwts.builder()
	                .subject(user.getEmail())
	                .claim("userId", user.getUserId())
	                .claim("role", user.getRole().toString())
	                .issuedAt(new Date(currentTime))
	                .expiration(new Date(expirationTime))
	                .signWith(key)
	                .compact();
	    }
	   
	 public LocalDateTime getExpirationTime() {
	        return LocalDateTime.now().plusHours(1);
	    }
}
