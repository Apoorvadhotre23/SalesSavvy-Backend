package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Jwt_Tokens;
import com.example.demo.entity.Users;

public interface JwtTokensRepository extends JpaRepository<Jwt_Tokens, Integer>{

	Optional<Jwt_Tokens> findByUser(Users user);
	
}
