package com.example.demo.service;

import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.LoginDTO;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;

@Service
@Transactional
public class AuthenticationService {

	UserRepository userRepo;
	PasswordEncoder passwordEncoder;
	JwtService jwtService;
	
	
	public AuthenticationService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepo = userRepo;
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
	        return userRepo.save(user);
	        
	 }      
	
	// Login
	 
	 public Users loginUser(LoginDTO userCredentials, HttpServletResponse response) {

		    // 1. Find user by email
	        Optional<Users> optionalUser =
	                userRepo.findByEmail(userCredentials.getEmail());
	        
	        // 2. Check whether user exists
	        if (optionalUser.isEmpty()) {
	            throw new RuntimeException("Invalid email or password");
	        }

	        Users user = optionalUser.get();

	        // 3. Check password
	        boolean passwordMatches =
	                passwordEncoder.matches(
	                        userCredentials.getPassword(),
	                        user.getPassword()
	                );

	        // 4. If password is wrong
	        if (!passwordMatches) {
	            throw new RuntimeException("Invalid password");
	        }
	        
	        // 5. Generate JWT
	        String token = jwtService.generateToken(user);

	        // 6. Add JWT to cookie
	        jwtService.addCookie(token, response);
	        
	        // 7. Return registered user
	        return user;
	    
	}

}