package com.example.demo.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LoginDTO;
import com.example.demo.entity.Users;
import com.example.demo.repository.JwtTokensRepository;
import com.example.demo.repository.UserRepository;

@Service
public class AuthenticationService {

	UserRepository userRepo;
	JwtTokensRepository jwtRepo;
	PasswordEncoder passwordEncoder;
	
	public AuthenticationService(UserRepository userRepo, JwtTokensRepository jwtRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.jwtRepo = jwtRepo;
		this.passwordEncoder = passwordEncoder;
	}
	
	// Registration
	 public Users registerUser(Users user) {

	        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
	            throw new RuntimeException("Username already exists");
	        }
	        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
	            throw new RuntimeException("Email already exists");
	        }
	        user.setPassword(
	                passwordEncoder.encode(user.getPassword())
	        );

	        return userRepo.save(user);
	    }
	
	// Login
	 
	 public Users loginUser(LoginDTO userCredentials) {

	        Optional<Users> optionalUser =
	                userRepo.findByEmail(userCredentials.getEmail());

	        if (optionalUser.isEmpty()) {
	            throw new RuntimeException("Invalid email or password");
	        }

	        Users user = optionalUser.get();

	        boolean passwordMatches =
	                passwordEncoder.matches(
	                        userCredentials.getPassword(),
	                        user.getPassword()
	                );

	        if (!passwordMatches) {
	            throw new RuntimeException("Invalid email or password");
	        }

	        return user;
		
	     
	}

}