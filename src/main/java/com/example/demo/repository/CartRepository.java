package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.CartItems;

public interface CartRepository extends JpaRepository<CartItems, Integer>{

	 // Find whether a product already exists in the user's cart
    @Query("SELECT c FROM CartItems c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    Optional<CartItems> findByUserAndProduct(
            @Param("userId") int userId,
            @Param("productId") int productId
    );

    // Count total quantities of all products in the user's cart
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItems c WHERE c.user.userId = :userId")
    int countTotalItems(@Param("userId") int userId);

    // Get all cart items belonging to a particular user
    @Query("SELECT c FROM CartItems c WHERE c.user.userId = :userId")
    List<CartItems> findByUserId(@Param("userId") int userId);
    
  
}
