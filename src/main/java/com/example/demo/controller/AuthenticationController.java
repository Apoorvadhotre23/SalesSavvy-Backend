package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginDTO;
import com.example.demo.entity.Users;
import com.example.demo.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthenticationController {
	
	AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
	
    @PostMapping("/signup")
    public Users registerUser(@RequestBody Users user) {
        return authenticationService.registerUser(user);
    }
    
    @PostMapping("/login")
    public Users loginUser(@RequestBody LoginDTO userCredentials) {
        return authenticationService.loginUser(userCredentials);
    }

}
