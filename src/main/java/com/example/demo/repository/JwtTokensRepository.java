package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Jwt_Tokens;

public interface JwtTokensRepository extends JpaRepository<Jwt_Tokens, Integer>{
	
}
