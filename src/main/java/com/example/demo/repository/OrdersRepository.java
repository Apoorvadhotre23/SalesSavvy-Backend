package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Orders;

public interface OrdersRepository extends JpaRepository<Orders, String>{

	 List<Orders> findByUser_UserIdOrderByCreatedAtDesc(int userId);
	 
	 Optional<Orders> findByRazorpayOrderId(String razorpayOrderId);
	 
	 
}
