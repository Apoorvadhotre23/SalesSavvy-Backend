package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CartItems;
import com.example.demo.entity.OrderItems;
import com.example.demo.entity.Orders;
import com.example.demo.entity.Products;
import com.example.demo.entity.Users;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemsRepository;
import com.example.demo.repository.OrdersRepository;
import com.example.demo.repository.ProductsRepository;
import com.example.demo.repository.UserRepository;

@Service
public class OrderService {
	
	    private final CartRepository cartRepository;
	    private final OrdersRepository ordersRepository;
	    private final OrderItemsRepository orderItemsRepository;
	    private final ProductsRepository productsRepository;
	    private final UserRepository userRepository;
	    

	    public OrderService(
	            CartRepository cartRepository,
	            OrdersRepository ordersRepository,
	            OrderItemsRepository orderItemsRepository,
	            ProductsRepository productsRepository,
	            UserRepository userRepository) {

	        this.cartRepository = cartRepository;
	        this.ordersRepository = ordersRepository;
	        this.orderItemsRepository = orderItemsRepository;
	        this.productsRepository = productsRepository;
	        this.userRepository = userRepository;
	    }

	    @Transactional
	    public Orders placeOrder(int userId, String recipientName, String phone, String addressLine,  String city, String state, String postalCode) { 
	    	
	    	
	    	Users user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("User not found"));
	    	
	    	// 2. Get cart items
	        List<CartItems> cartItems =
	                cartRepository.findByUserId(userId);
	        
	        if (cartItems.isEmpty()) {
	            throw new IllegalArgumentException("Your cart is empty");
	        }
	        
	        // 3. Create order
	        Orders order = new Orders();
	        
	        order.setOrderId("ORD-" + UUID.randomUUID());
	        order.setUser(user);
	        order.setStatus(OrderStatus.PENDING);
	        order.setPaymentMethod("COD");
	        order.setPaymentStatus(PaymentStatus.PENDING);

	        order.setRecipientName(recipientName);
	        order.setPhone(phone);
	        order.setAddressLine(addressLine);
	        order.setCity(city);
	        order.setState(state);
	        order.setPostalCode(postalCode);

	        BigDecimal totalAmount = BigDecimal.ZERO;
	        
	        
	     // 4. Validate stock and calculate total
	        for (CartItems cartItem : cartItems) {

	            Products product = productsRepository.findById(
	                    cartItem.getProduct().getProductId())
	                    .orElseThrow(() ->
	                        new IllegalArgumentException("Product not found"));

	            int quantity = cartItem.getQuantity();

	            if (quantity <= 0 || quantity > product.getStock()) {
	                throw new IllegalArgumentException(
	                        "Insufficient stock for " + product.getName());
	            }

	            BigDecimal itemTotal = product.getPrice()
	                    .multiply(BigDecimal.valueOf(quantity));

	            totalAmount = totalAmount.add(itemTotal);
	        }
	        
	        // 5. Save order total
	        order.setTotalAmount(totalAmount);
	        order = ordersRepository.save(order);
	        
	     // 6. Save order items and reduce stock
	        for (CartItems cartItem : cartItems) {

	            Products product = productsRepository.findById(
	                    cartItem.getProduct().getProductId())
	                    .orElseThrow(() ->
	                        new IllegalArgumentException("Product not found"));

	            int quantity = cartItem.getQuantity();

	            BigDecimal pricePerUnit = product.getPrice();

	            BigDecimal itemTotal = pricePerUnit
	                    .multiply(BigDecimal.valueOf(quantity));

	            OrderItems orderItem = new OrderItems();

	            orderItem.setOrder(order);
	            orderItem.setProduct(product);
	            orderItem.setQuantity(quantity);
	            orderItem.setPricePerUnit(pricePerUnit);
	            orderItem.setTotalPrice(itemTotal);

	            orderItemsRepository.save(orderItem);

	            // Reduce available stock
	            product.setStock(product.getStock() - quantity);
	            productsRepository.save(product);
	        }

	        // 7. Clear cart after successful order creation
	        cartRepository.deleteByUser_UserId(userId);

	        
	    	return order;
	    }
}
