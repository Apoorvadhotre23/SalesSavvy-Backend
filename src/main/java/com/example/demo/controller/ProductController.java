package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Products;
import com.example.demo.entity.Users;
import com.example.demo.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	
	ProductService productService;
	
	public ProductController(ProductService productService) {
		this.productService =  productService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> getProducts(@RequestParam String category, HttpServletRequest request) {
	 try {	
		Users authenticatedUser = (Users) request.getAttribute("authenticatedUser");
		if(authenticatedUser == null) {
			return ResponseEntity.status(401).body(Map.of("Error", "Unauthorized Access"));
		}
		
		// get data from service
		List<Products> products  = productService.getProductsCategory(category);
		
		// Creating response Map
		Map<String, Object> response = new HashMap<>();
		
		// Creating user Map
		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("name", authenticatedUser.getUsername());
		userInfo.put("role", authenticatedUser.getRole().name());
		
		// Adding user info in response Map
		response.put("user", userInfo);
		
		// Creating product info map
		List<Map<String, Object>> productList = new ArrayList<>();
		
		for (Products product : products) {
			Map<String, Object> productDetails = new HashMap<>();
			productDetails.put("productId", product.getProductId());
			productDetails.put("name", product.getName());
			productDetails.put("price", product.getPrice());
			productDetails.put("description", product.getDescription());
			productDetails.put("stock", product.getStock());
			
			List<String> images = productService.getProductImage(product.getProductId());
			
			productDetails.put("image", images);
			
			productList.add(productDetails);
		}
		
		response.put("products", productList);
		
		return ResponseEntity.ok(response);
		
	    }catch(Exception e) {
	    	return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
	   }
	}
	
	
	
	
	// Products Details Controller
	
	

@GetMapping("/{productId}")
public ResponseEntity<Map<String, Object>> getProductById(
        @PathVariable Integer productId,
        HttpServletRequest request) {

    Users authenticatedUser =
            (Users) request.getAttribute("authenticatedUser");

    if (authenticatedUser == null) {
        return ResponseEntity.status(401)
                .body(Map.of("error", "Unauthorized Access"));
    }

    Optional<Products> optionalProduct =
            productService.getProductById(productId);

    if (optionalProduct.isEmpty()) {
        return ResponseEntity.status(404)
                .body(Map.of("error", "Product not found"));
    }

    Products product = optionalProduct.get();

    Map<String, Object> productDetails = new HashMap<>();

    productDetails.put("productId", product.getProductId());
    productDetails.put("name", product.getName());
    productDetails.put("description", product.getDescription());
    productDetails.put("price", product.getPrice());
    productDetails.put("stock", product.getStock());

    List<String> images =
            productService.getProductImage(product.getProductId());

    productDetails.put("image", images);

    return ResponseEntity.ok(productDetails);
}

}
