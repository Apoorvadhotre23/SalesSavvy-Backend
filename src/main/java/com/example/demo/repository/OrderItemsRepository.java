package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.OrderItems;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Integer> {

	List<OrderItems> findByOrder_OrderId(String orderId);
}
