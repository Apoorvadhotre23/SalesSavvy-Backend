package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepo;
	private final BCryptPasswordEncoder passwordEncoder;
	
	
	public UserService(UserRepository userRepo) {
		this.userRepo = userRepo;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}
	
	public Users registerUser(Users user) {
		if(userRepo.findByUsername(user.getUsername()).isPresent()) {
			throw new RuntimeException("User name is already taken");
		}if(userRepo.findByEmail(user.getEmail()).isPresent()) {
			throw new RuntimeException("Email is already taken");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
		return userRepo.save(user);
	}
}
