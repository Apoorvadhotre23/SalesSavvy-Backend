package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Products;


@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer>{

	public List<Products> findByCategory_CategoryId(Integer categoryId);
	
	@Query("SELECT p.category.categoryName from Products p where p.productId = :productId")
	String findCategoryNameByProductId(int productId);
}
