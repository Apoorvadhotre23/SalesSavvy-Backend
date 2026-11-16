
package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.entity.Orders;
import com.example.demo.entity.Users;
import com.example.demo.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestBody OrderRequestDTO request,
            HttpServletRequest httpRequest) {

        try {

            // Get logged-in user from AuthenticationFilter
            Users user = (Users) httpRequest.getAttribute("authenticatedUser");

            if (user == null) {
                return ResponseEntity.status(401)
                        .body("User not authenticated");
            }
            
            int userId = user.getUserId();
            
            Orders order = orderService.placeOrder(
                    userId,
                    request.getRecipientName(),
                    request.getPhone(),
                    request.getAddressLine(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode()
            );

            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
