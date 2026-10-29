package com.example.demo.filter;

import java.io.IOException;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Role;
import com.example.demo.entity.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.JwtService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationFilter implements Filter{
	
	AuthenticationService authservice;
	UserRepository userRepo;
	JwtService jwtService;
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
	private static final String ALLOWED_ORIGIN = "http://localhost:5173";
	
	public static final String[] UNAUTHENTICATED_PATHS = {
			"/api/auth/signup",
			"/api/auth/login",
			"/api/auth/logout"
	};
	
	public AuthenticationFilter(AuthenticationService authservice, UserRepository userRepo, JwtService jwtService) {
		this.authservice = authservice;
		this.userRepo = userRepo;
		this.jwtService = jwtService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
			
		try {
			System.out.println("Filter started");
			executeFilterLogin(request, response, chain);
			
		} catch (Exception e) {
		    logger.error("Unexpected Error in Authentication Filter", e);

		    HttpServletResponse httpresponse =
		            (HttpServletResponse) response;

		    if (!httpresponse.isCommitted()) {
		        httpresponse.resetBuffer();

		        sendErrorResponse(
		            httpresponse,
		            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		            "Internal Server Error"
		        );
		    }
		}
		
	}
	

	public void executeFilterLogin(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String requestURI = httpRequest.getRequestURI();
		logger.info("Request URI : {} ", requestURI);
		
		if (requestURI.startsWith("/api/")) {

		    String origin = httpRequest.getHeader("Origin");

		    if (ALLOWED_ORIGIN.equals(origin)) {
		        setCORSHeaders(httpResponse);
		    }

		    if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {

		        if (!ALLOWED_ORIGIN.equals(origin)) {
		            httpResponse.sendError(
		                HttpServletResponse.SC_FORBIDDEN
		            );
		            return;
		        }

		        httpResponse.setStatus(HttpServletResponse.SC_OK);
		        return;
		    }
		}
		
		if(Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
			chain.doFilter(request, response);
			return;
		}
		
		//we have to validate token
		//fetch the token from cookie present in request
		String token = getAuthTokenFromCookie(httpRequest);
		
		if (token == null || token.isBlank()) {
		    sendErrorResponse(
		        httpResponse,
		        HttpServletResponse.SC_UNAUTHORIZED,
		        "Unauthorized: Please login first"
		    );
		    return;
		}
		
		boolean validation;

		try {
		    validation = jwtService.validateToken(token);
		} catch (Exception e) {
		    validation = false;
		}

		if (!validation) {
		    sendErrorResponse(
		        httpResponse,
		        HttpServletResponse.SC_UNAUTHORIZED,
		        "Unauthorized: Invalid or expired token"
		    );
		    return;
		}
		

		String username;

		try {
		    username = jwtService.extractUsername(token);

		    if (username == null || username.isBlank()) {
		        sendErrorResponse(
		            httpResponse,
		            HttpServletResponse.SC_UNAUTHORIZED,
		            "Unauthorized: Invalid token"
		        );
		        return;
		    }

		} catch (Exception e) {
		    sendErrorResponse(
		        httpResponse,
		        HttpServletResponse.SC_UNAUTHORIZED,
		        "Unauthorized: Invalid token"
		    );
		    return;
		}

		Optional<Users> optionalUser = userRepo.findByUsername(username);
		
		
		if(optionalUser.isEmpty()) {
			sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: UserNotFound");
			return;
		}
		
		Users authenticatedUser = optionalUser.get();
		Role role = authenticatedUser.getRole();
		logger.info("Authenticated User : {} , Role : {} ", authenticatedUser.getUsername(), role);
		
		
		
		
		if(requestURI.startsWith("/api") && (role != Role.ADMIN && role != Role.CUSTOMER)) {
			sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Unauthorized: Not Allowed for Role");
			return;
		}
		
		httpRequest.setAttribute("authenticatedUser", authenticatedUser);
		chain.doFilter(request, response);
	}

	
    private void sendErrorResponse(HttpServletResponse response, int statuscode, String message) throws IOException {
		response.setStatus(statuscode);
		response.getWriter().write(message);
		
	}

	public String getAuthTokenFromCookie(HttpServletRequest request) {

	    Cookie[] cookies = request.getCookies();

	    if (cookies != null) {

	        for (Cookie cookie : cookies) {

	            if ("jwt".equals(cookie.getName())) {

	                String token = cookie.getValue();

	                return token;
	            }
	        }
	    }

	    return null;
	}
	
	private void setCORSHeaders(HttpServletResponse response) {

	    response.setHeader(
	        "Access-Control-Allow-Origin",
	        ALLOWED_ORIGIN
	    );

	    response.setHeader(
	        "Access-Control-Allow-Methods",
	        "GET, POST, PUT, DELETE, OPTIONS"
	    );

	    response.setHeader(
	        "Access-Control-Allow-Headers",
	        "Content-Type, Authorization"
	    );

	    response.setHeader(
	        "Access-Control-Allow-Credentials",
	        "true"
	    );

	    response.setHeader("Vary", "Origin");
	}
}
