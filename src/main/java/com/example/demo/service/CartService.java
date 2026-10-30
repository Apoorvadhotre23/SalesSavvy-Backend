
package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CartItems;
import com.example.demo.entity.Products;
import com.example.demo.entity.Users;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductsRepository;
import com.example.demo.repository.UserRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductsRepository productRepository;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       ProductsRepository productRepository) {

        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }
    
    // Count of items

    public int getCount(int userId) {
        return cartRepository.countTotalItems(userId);
    }
    
    // add method

    @Transactional
    public void addToCart(int userId, int productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<CartItems> existingItem =
                cartRepository.findByUserAndProduct(userId, productId);

        if (existingItem.isPresent()) {

            CartItems cartItem = existingItem.get();

            long newQuantity = (long) cartItem.getQuantity() + quantity;

            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException("Not enough stock");
            }

            cartItem.setQuantity((int) newQuantity);
            cartRepository.save(cartItem);

        } else {

            if (quantity > product.getStock()) {
                throw new IllegalArgumentException("Not enough stock");
            }

            CartItems newItem = new CartItems(user, product, quantity);
            cartRepository.save(newItem);
        }
    }
    
    
    // List of items 
    
    public List<CartItems> getCartItems(int userId) {
        return cartRepository.findByUserId(userId);
    }
    
    
    // update quantity 
    





}
