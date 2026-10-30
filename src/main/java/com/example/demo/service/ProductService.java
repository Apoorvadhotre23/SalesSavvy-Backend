package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Categories;
import com.example.demo.entity.ProductImages;
import com.example.demo.entity.Products;
import com.example.demo.repository.CategoriesRepository;
import com.example.demo.repository.ProductImagesRepository;
import com.example.demo.repository.ProductsRepository;

@Service
public class ProductService {
	
	ProductsRepository productsRepository;
	ProductImagesRepository productImagesRepository;
	CategoriesRepository categoriesRepository;
	
	public ProductService(ProductsRepository productsRepository, ProductImagesRepository productImagesRepository, CategoriesRepository categoriesRepository) {
			this.productsRepository =  productsRepository;
			this.productImagesRepository = productImagesRepository;
			this.categoriesRepository = categoriesRepository;
	}
	
	public List<Products> getProductsCategory(String categoryName) {
	  if(categoryName != null && !categoryName.isEmpty()) {
		Optional<Categories> optionalCategory = categoriesRepository.findByCategoryName(categoryName);
			if(optionalCategory.isPresent()) {
				Categories category = optionalCategory.get();
				return productsRepository.findByCategory_CategoryId(category.getCategoryId());
			}else {
				throw new RuntimeException("Category Not found");
			}
		
		} else {
			return productsRepository.findAll();
		}
		
		
	}
	
	public List<String> getProductImage(Integer productId) {
		List<ProductImages> productImgaes =  productImagesRepository.findByProduct_productId(productId);
		List<String> imageUrls = new ArrayList<>();
		
		for (ProductImages image : productImgaes) {
			imageUrls.add(image.getImageUrl());
		}
		return imageUrls;
	}

	
	public Optional<Products> getProductById(Integer productId) {
	    return productsRepository.findById(productId);
	}
	
	

	public List<Products> searchProducts(String keyword) {

		if (keyword == null || keyword.trim().isEmpty()) {
			return new ArrayList<>();
		}

		String searchText = keyword.trim().toLowerCase();

		// Handle common singular/plural category searches
		if (searchText.equals("shirts")) {
			searchText = "shirt";
		} else if (searchText.equals("mobiles")) {
			searchText = "mobile";
		} else if (searchText.equals("pants")) {
			searchText = "pant";
		} else if (searchText.equals("accessory")
		        || searchText.equals("accessories")
		        || searchText.equals("accesories")) {
		    searchText = "accesories";
		}

		return productsRepository.searchProducts(searchText);
	}

}
