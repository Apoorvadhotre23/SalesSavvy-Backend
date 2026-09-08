package com.example.demo.service;

import java.util.Optional;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LoginDTO;
import com.example.demo.entity.Jwt_Tokens;
import com.example.demo.entity.Users;
import com.example.demo.repository.JwtTokensRepository;
import com.example.demo.repository.UserRepository;

@Service
@Transactional
public class AuthenticationService {

	UserRepository userRepo;
	JwtTokensRepository jwtRepo;
	PasswordEncoder passwordEncoder;
	JwtService jwtService;
	
	
	public AuthenticationService(UserRepository userRepo, JwtTokensRepository jwtRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepo = userRepo;
		this.jwtRepo = jwtRepo;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}
	
	// Registration
	 public Users registerUser(Users user) {
		 	
		    // 1. Check user name
	        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
	            throw new RuntimeException("Username already exists");
	        }
	        
	        // 2. Check email
	        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
	            throw new RuntimeException("Email already exists");
	        }
	        
	        // 3. Encrypt password
	        user.setPassword(
	                passwordEncoder.encode(user.getPassword())
	        );
	        
	        // 4. Save user
	        Users registeredUser = userRepo.save(user);
	        
	        // 5. Generate JWT
	        String token = jwtService.generateToken(registeredUser);
	        
	     // 6. Create Jwt_Tokens object
	        Jwt_Tokens jwtToken = new Jwt_Tokens();
	        
	        jwtToken.setUser(registeredUser);
	        jwtToken.setToken(token);
	        jwtToken.setExpires_at(
	                jwtService.getExpirationTime()
	        );
	        
	        // 7. Save token
	        jwtRepo.save(jwtToken);

	        // 8. Return registered user
	        return registeredUser;
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