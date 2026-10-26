package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.entity.Users;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	  @GetMapping("/me")
	    public ResponseEntity<?> getMyProfile(
	            HttpServletRequest request) {

	        Users user = (Users) request.getAttribute("authenticatedUser");

	        if (user == null) {
	            return ResponseEntity
	                    .status(HttpStatus.UNAUTHORIZED)
	                    .body("Please login first");
	        }

	        UserProfileDTO profile = new UserProfileDTO(
	                user.getUserId(),
	                user.getUsername(),
	                user.getEmail(),
	                user.getRole().name()
	        );

	        return ResponseEntity.ok(profile);
	  }

}
