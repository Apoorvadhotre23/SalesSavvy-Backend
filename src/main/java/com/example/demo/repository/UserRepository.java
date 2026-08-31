package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Users;
import java.util.Optional;


public interface UserRepository extends JpaRepository<Users, Integer>{
	 Optional<Users> findByEmail(String email);
	 Optional<Users> findByUsername(String username);
}
