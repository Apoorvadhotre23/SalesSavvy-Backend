package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Users;
import com.example.demo.service.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:5173/")
@RequestMapping("/api/users")
public class UsersController {
	
	UserService userService;
	
	public UsersController(UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody Users user) {
		try {
			
			Users registredUser = userService.registerUser(user);
			return ResponseEntity.ok(Map.of("message","User registred succesfully", "user", registredUser));
			
		} catch (RuntimeException e) {
			 return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
