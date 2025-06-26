package com.dealkartbd.backend_app.security;

import com.dealkartbd.backend_app.dto.login.LoginRequest;
import com.dealkartbd.backend_app.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class AuthFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    public AuthFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
    }
   @Override
   public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
           throws AuthenticationException {
       try {
           LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
           UsernamePasswordAuthenticationToken authenticationToken =
                   new UsernamePasswordAuthenticationToken(
                           loginRequest.email(),
                           loginRequest.password()
                   );
           setDetails(request, authenticationToken);
           return this.getAuthenticationManager().authenticate(authenticationToken);
       } catch (IOException e) {
           throw new RuntimeException("Failed to parse authentication request body", e);
       }
   }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        // Generate JWT token (example implementation)
        String username = authResult.getName();

        // You should use a proper JWT library and secret key management in production
        String token = jwtUtil.generateToken(username, authResult.getAuthorities());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\": \"" + token + "\"}");
    }
}
