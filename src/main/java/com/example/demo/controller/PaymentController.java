package com.example.demo.controller;


import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Orders;
import com.example.demo.entity.Users;
import com.example.demo.service.PaymentService;
import com.razorpay.RazorpayException;
import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.dto.PaymentVerificationRequestDTO;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 1. Create Razorpay Order
    @PostMapping("/create-order")
    public ResponseEntity<?> createPaymentOrder(
            HttpServletRequest request,
            @RequestBody OrderRequestDTO orderRequest) {

        Users user = (Users) request.getAttribute("authenticatedUser");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first");
        }

        
		try {
			Map<String, Object>	response = paymentService.createRazorpayOrder(
			        user.getUserId(),
			        orderRequest.getRecipientName(),
			        orderRequest.getPhone(),
			        orderRequest.getAddressLine(),
			        orderRequest.getCity(),
			        orderRequest.getState(),
			        orderRequest.getPostalCode()
			        
			);
			
			 return ResponseEntity.ok(response);
		} catch (RazorpayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
	                .body("Unable to create Razorpay order");
		}


       
    }

    // 2. Verify Razorpay Payment
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody PaymentVerificationRequestDTO paymentRequest,
            HttpServletRequest request) {

        Users user = (Users) request.getAttribute("authenticatedUser");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please login first");
        }

        
		try {
			Orders order = paymentService.verifyPayment(
			        user.getUserId(),
			        paymentRequest.getRazorpayOrderId(),
			        paymentRequest.getRazorpayPaymentId(),
			        paymentRequest.getRazorpaySignature()
			);
	        return ResponseEntity.ok(order);
		} catch (RazorpayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
	                .body("Unable to verify Razorpay payment");
		}


    }
}
