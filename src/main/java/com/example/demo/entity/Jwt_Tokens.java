package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jwt_tokens")
public class Jwt_Tokens {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private int token_id;
	
	    @ManyToOne
	    @JoinColumn(name = "user_id", nullable = false)
	    private Users user;

	    @Column(name = "token", nullable = false, length = 255)
	    private String token;

	    @Column(name = "created_at", insertable = false, updatable = false)
	    private LocalDateTime created_at;

	    @Column(name = "expires_at", nullable = false)
	    private LocalDateTime expires_at;
	    
	    public Jwt_Tokens() {
			
		}

		public Jwt_Tokens(int token_id, Users user, String token, LocalDateTime created_at, LocalDateTime expires_at) {
			super();
			this.token_id = token_id;
			this.user = user;
			this.token = token;
			this.created_at = created_at;
			this.expires_at = expires_at;
		}

		public int getToken_id() {
			return token_id;
		}

		public void setToken_id(int token_id) {
			this.token_id = token_id;
		}

		public Users getUser() {
			return user;
		}

		public void setUser(Users user) {
			this.user = user;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public LocalDateTime getCreated_at() {
			return created_at;
		}

		public void setCreated_at(LocalDateTime created_at) {
			this.created_at = created_at;
		}

		public LocalDateTime getExpires_at() {
			return expires_at;
		}

		public void setExpires_at(LocalDateTime expires_at) {
			this.expires_at = expires_at;
		}
	    
	    
}
