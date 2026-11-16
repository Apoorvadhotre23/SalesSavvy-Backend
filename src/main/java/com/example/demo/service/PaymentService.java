package com.example.demo.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final CartRepository cartRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductsRepository productsRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    public PaymentService(
            RazorpayClient razorpayClient,
            CartRepository cartRepository,
            OrdersRepository ordersRepository,
            OrderItemsRepository orderItemsRepository,
            ProductsRepository productsRepository,
            UserRepository userRepository) {

        this.razorpayClient = razorpayClient;
        this.cartRepository = cartRepository;
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.productsRepository = productsRepository;
        this.userRepository = userRepository;
    }

    // 1. Create Razorpay Order
    @Transactional
    public Map<String, Object> createRazorpayOrder(
            int userId,
            String recipientName,
            String phone,
            String addressLine,
            String city,
            String state,
            String postalCode) throws RazorpayException {

        Users user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<CartItems> cartItems =
                cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Calculate total using database product prices
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
        
        

     // Check whether an existing pending order can be reused
     Orders existingOrder = findReusablePendingOrder(
             userId,
             cartItems,
             totalAmount,
             recipientName,
             phone,
             addressLine,
             city,
             state,
             postalCode
     );

     if (existingOrder != null) {

         // Reuse the existing SaleSavvy and Razorpay order IDs
         long amountInPaise = existingOrder.getTotalAmount()
                 .multiply(BigDecimal.valueOf(100))
                 .longValueExact();

         Map<String, Object> response = new HashMap<>();

         response.put("saleSavvyOrderId", existingOrder.getOrderId());
         response.put("razorpayOrderId", existingOrder.getRazorpayOrderId());
         response.put("amount", amountInPaise);
         response.put("currency", "INR");
         response.put("keyId", keyId);

         return response;
     }


        // Create SaleSavvy pending order
        Orders order = new Orders();

        order.setOrderId("ORD-" + UUID.randomUUID());
        order.setUser(user);
        order.setTotalAmount(totalAmount);

        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod("RAZORPAY");
        order.setPaymentStatus(PaymentStatus.PENDING);

        order.setRecipientName(recipientName);
        order.setPhone(phone);
        order.setAddressLine(addressLine);
        order.setCity(city);
        order.setState(state);
        order.setPostalCode(postalCode);

        order = ordersRepository.save(order);

        // Save order items without reducing stock
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
        }

        // Razorpay accepts amount in paise
        long amountInPaise = totalAmount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        JSONObject razorpayRequest = new JSONObject();

        razorpayRequest.put("amount", amountInPaise);
        razorpayRequest.put("currency", "INR");
        razorpayRequest.put("receipt", order.getOrderId());

        // Create order in Razorpay
        com.razorpay.Order razorpayOrder =
                razorpayClient.orders.create(razorpayRequest);

        String razorpayOrderId = razorpayOrder.get("id");

        // Store Razorpay order ID in SaleSavvy database
        order.setRazorpayOrderId(razorpayOrderId);
        ordersRepository.save(order);

        // Send details to React
        Map<String, Object> response = new HashMap<>();

        response.put("saleSavvyOrderId", order.getOrderId());
        response.put("razorpayOrderId", razorpayOrderId);
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        response.put("keyId", keyId);

        return response;
    }

   

	// 2. Verify Razorpay Payment
    @Transactional
    public Orders verifyPayment(
            int userId,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) throws RazorpayException {

        Orders order = ordersRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        // Ensure this order belongs to the logged-in user
        if (order.getUser().getUserId() != userId) {
            throw new SecurityException("Unauthorized order access");
        }

        // Prevent processing the same successful payment twice
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return order;
        }

        // Verify Razorpay signature
        boolean signatureValid = Utils.verifySignature(
                razorpayOrderId + "|" + razorpayPaymentId,
                razorpaySignature,
                keySecret
        );

        if (!signatureValid) {
            throw new SecurityException("Invalid payment signature");
        }

        // Fetch payment details directly from Razorpay
        Payment payment = razorpayClient.payments.fetch(razorpayPaymentId);

        String paymentOrderId = payment.get("order_id");
        String paymentCurrency = payment.get("currency");
        String paymentStatus = payment.get("status");

        long paidAmount = ((Number) payment.get("amount")).longValue();

        long expectedAmount = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        // Verify payment details
        if (!razorpayOrderId.equals(paymentOrderId)
                || paidAmount != expectedAmount
                || !"INR".equals(paymentCurrency)
                || !"captured".equals(paymentStatus)) {

            throw new IllegalArgumentException(
                    "Payment details could not be verified");
        }

        // Get saved order items
        List<OrderItems> orderItems =
                orderItemsRepository.findByOrder_OrderId(
                        order.getOrderId());

        // Recheck stock before finalizing the order
        for (OrderItems orderItem : orderItems) {

            Products product = productsRepository.findById(
                    orderItem.getProduct().getProductId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Product not found"));

            if (orderItem.getQuantity() > product.getStock()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for " + product.getName());
            }
        }

        // Reduce stock only after verified payment
        for (OrderItems orderItem : orderItems) {

            Products product = productsRepository.findById(
                    orderItem.getProduct().getProductId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Product not found"));

            product.setStock(
                    product.getStock() - orderItem.getQuantity());

            productsRepository.save(product);
        }

        // Update SaleSavvy order status
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setStatus(OrderStatus.SUCCESS);

        // Save Razorpay payment ID
        // We will add a separate database field for this later if needed.

        ordersRepository.save(order);

        // Clear customer's cart after successful payment
        cartRepository.deleteByUser_UserId(userId);

        return order;
    }
    
    

 // Find an existing pending Razorpay order that matches the current checkout
 private Orders findReusablePendingOrder(
         int userId,
         List<CartItems> cartItems,
         BigDecimal totalAmount,
         String recipientName,
         String phone,
         String addressLine,
         String city,
         String state,
         String postalCode) {

     List<Orders> previousOrders =
             ordersRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

     // Build current cart snapshot
     Map<Object, Integer> currentCart = new HashMap<>();

     for (CartItems item : cartItems) {
         currentCart.put(
                 item.getProduct().getProductId(),
                 item.getQuantity()
         );
     }

     // Check previous orders
     for (Orders order : previousOrders) {

         if (order.getStatus() != OrderStatus.PENDING
                 || order.getPaymentStatus() != PaymentStatus.PENDING
                 || !"RAZORPAY".equals(order.getPaymentMethod())
                 || order.getRazorpayOrderId() == null) {

             continue;
         }

         // Check total amount
         if (order.getTotalAmount().compareTo(totalAmount) != 0) {
             continue;
         }

         // Check delivery details
         if (!java.util.Objects.equals(order.getRecipientName(), recipientName)
                 || !java.util.Objects.equals(order.getPhone(), phone)
                 || !java.util.Objects.equals(order.getAddressLine(), addressLine)
                 || !java.util.Objects.equals(order.getCity(), city)
                 || !java.util.Objects.equals(order.getState(), state)
                 || !java.util.Objects.equals(order.getPostalCode(), postalCode)) {

             continue;
         }

         // Fetch previously saved order items
         List<OrderItems> previousItems =
                 orderItemsRepository.findByOrder_OrderId(order.getOrderId());

         // Build previous order cart snapshot
         Map<Object, Integer> previousCart = new HashMap<>();

         for (OrderItems item : previousItems) {
             previousCart.put(
                     item.getProduct().getProductId(),
                     item.getQuantity()
             );
         }

         // Reuse only if the cart contents are identical
         if (currentCart.equals(previousCart)) {
             return order;
         }
     }

     return null;
 }

}
