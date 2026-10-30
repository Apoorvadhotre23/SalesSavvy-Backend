package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.CartItems;
import com.example.demo.entity.Products;
import com.example.demo.entity.Users;
import com.example.demo.service.CartService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	
	private CartService cartService;
	
	public CartController(CartService cartService) {
		this.cartService =  cartService;
	}

	// Count logic is implemented 
	
	  @GetMapping("/count")
	    public ResponseEntity<?> getCartCount(HttpServletRequest request) {

	        Users authenticatedUser =
	                (Users) request.getAttribute("authenticatedUser");

	        if (authenticatedUser == null) {
	            return ResponseEntity.status(401)
	                    .body(Map.of("error", "Please login first"));
	        }

	        int userId = authenticatedUser.getUserId();

	        int count = cartService.getCount(userId);

	        return ResponseEntity.ok(Map.of("count", count));
	    }
	  
	  
	  // addToCart logic is implemented

	  @PostMapping("/add")
	  public ResponseEntity<?> addToCart(
			  @RequestBody Map<String, Integer> requestBody,
			  HttpServletRequest request) {

		  	  Users authenticatedUser = (Users) request.getAttribute("authenticatedUser");

		  	  if (authenticatedUser == null) {
		  		  	return ResponseEntity.status(401).body(Map.of("error", "Please login first"));
		  	  }

		  	  Integer productId = requestBody.get("productId");
		  	  Integer quantity = requestBody.get("quantity");

		  	  if (productId == null || quantity == null) {
		  		  return ResponseEntity.badRequest().body(Map.of("error", "Product ID and quantity are required"));
		  	  }

		  	  try {
		  		 cartService.addToCart(authenticatedUser.getUserId(),productId,quantity);

		  		 return ResponseEntity.ok(Map.of("message", "Product added to cart successfully"));

		  	  } catch (IllegalArgumentException e) {
		  		  return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		  	  }
	  }

	  
	  // GetCartItems logic is created
	  

@GetMapping
public ResponseEntity<?> getCartItems(HttpServletRequest request) {

    Users authenticatedUser =
            (Users) request.getAttribute("authenticatedUser");

    if (authenticatedUser == null) {
        return ResponseEntity.status(401)
                .body(Map.of("error", "Please login first"));
    }

    int userId = authenticatedUser.getUserId();

    List<CartItems> cartItems = cartService.getCartItems(userId);

    List<Map<String, Object>> cartList = new ArrayList<>();

    for (CartItems item : cartItems) {

        Products product = item.getProduct();

        Map<String, Object> cartDetails = new HashMap<>();

        cartDetails.put("cartItemId", item.getId());
        cartDetails.put("productId", product.getProductId());
        cartDetails.put("name", product.getName());
        cartDetails.put("price", product.getPrice());
        cartDetails.put("quantity", item.getQuantity());
        cartDetails.put("stock", product.getStock());

        cartList.add(cartDetails);
    }

    return ResponseEntity.ok(Map.of("cartItems", cartList));
}

	  
// Update cartItems logic is created 

@PutMapping("/update/{cartItemId}")
public ResponseEntity<?> updateCartQuantity(
        @PathVariable int cartItemId,
        @RequestBody Map<String, Integer> requestBody,
        HttpServletRequest request) {

    Users authenticatedUser =
            (Users) request.getAttribute("authenticatedUser");

    if (authenticatedUser == null) {
        return ResponseEntity.status(401)
                .body(Map.of("error", "Please login first"));
    }

    Integer quantity = requestBody.get("quantity");

    if (quantity == null) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Quantity is required"));
    }

    try {
        cartService.updateQuantity(
                authenticatedUser.getUserId(),
                cartItemId,
                quantity
        );

        return ResponseEntity.ok(
                Map.of("message", "Cart quantity updated successfully")
        );

    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}


// remove cart item logic is implemented


@DeleteMapping("/remove/{cartItemId}")
public ResponseEntity<?> removeFromCart(
        @PathVariable int cartItemId,
        HttpServletRequest request) {

    Users authenticatedUser =
            (Users) request.getAttribute("authenticatedUser");

    if (authenticatedUser == null) {
        return ResponseEntity.status(401)
                .body(Map.of("error", "Please login first"));
    }

    try {
        cartService.removeFromCart(
                authenticatedUser.getUserId(),
                cartItemId
        );

        return ResponseEntity.ok(
                Map.of("message", "Product removed from cart successfully")
        );

    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}


}
