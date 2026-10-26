package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Categories {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "category_id")
	    private int categoryId;

	    @Column(name = "category_name", nullable = false, unique = true)
	    private String categoryName;
	    
	    // Default constructor
	    public Categories() {
			// TODO Auto-generated constructor stub
		}

		public Categories(int categoryId, String categoryName) {
			super();
			this.categoryId = categoryId;
			this.categoryName = categoryName;
		}

		public Categories(String categoryName) {
			super();
			this.categoryName = categoryName;
		}

		
		//Getters and Setters 
		
		public int getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(int categoryId) {
			this.categoryId = categoryId;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}
	    
	    
		
		

}
