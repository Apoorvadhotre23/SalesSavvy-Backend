package com.example.demo.service;

import com.example.demo.dto.LoginDTO;
import com.example.demo.repository.UserRepository;

public class AuthenticationService {

	UserRepository userRepo;
	
	public AuthenticationService(UserRepository userRepo) {
		this.userRepo = userRepo;
	}
	
	public void loginUser(LoginDTO userCre) {
		
		// 1. Find user using email
	}
}
