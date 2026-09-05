package com.example.demo.service;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtService {
	
	   SecretKey key = Jwts.SIG.HS256.key().build();
	   
	   
}
