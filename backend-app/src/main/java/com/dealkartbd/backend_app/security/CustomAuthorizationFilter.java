package com.dealkartbd.backend_app.security;

import com.dealkartbd.backend_app.dto.UserDto;
import com.dealkartbd.backend_app.entity.AccountStatus;
import com.dealkartbd.backend_app.exception.ApiErrorResponse;
import com.dealkartbd.backend_app.exception.CustomAccountStatusException;
import com.dealkartbd.backend_app.exception.TokenInvalidException;
import com.dealkartbd.backend_app.service.CustomUserDetailsService;
import com.dealkartbd.backend_app.service.UserService;
import com.dealkartbd.backend_app.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.dealkartbd.backend_app.constans.ApiPaths.CONFIRM_EMAIL_PATH;
import static com.dealkartbd.backend_app.constans.ApiPaths.HOME_PATH;
import static com.dealkartbd.backend_app.constans.AppConstants.ACCESS_DENIED_MSG_WITH_COLON;
import static com.dealkartbd.backend_app.constans.AppConstants.ERROR;
import static com.dealkartbd.backend_app.exception.ErrorMessages.*;
import static com.dealkartbd.backend_app.security.SecurityConstants.*;

@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final Environment env;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (servletPath.equals(env.getProperty(LOGIN_PATH)) ||
            servletPath.equals(HOME_PATH) ||
            servletPath.startsWith(CONFIRM_EMAIL_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            writeErrorResponse(response, TOKEN_IS_EXPIRED.getMessage(), HttpServletResponse.SC_UNAUTHORIZED,
                    request.getRequestURI());
        } catch (TokenInvalidException | IOException ex) {
            writeErrorResponse(response, TOKEN_IS_NOT_VALID.getMessage(), HttpServletResponse.SC_UNAUTHORIZED,
                    request.getRequestURI());
        } catch (Exception ex) {
            writeErrorResponse(response, ACCESS_DENIED_MSG_WITH_COLON + ex.getMessage(),
                    HttpServletResponse.SC_FORBIDDEN, request.getRequestURI());
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
            throws IOException, TokenInvalidException, ExpiredJwtException {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (token == null) return null;
        token = token.substring(BEARER_PREFIX.length());

        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (ExpiredJwtException | TokenInvalidException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TokenInvalidException(TOKEN_IS_NOT_VALID.getMessage());
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtUtil.validateToken(token, userDetails)) {
                throw new TokenInvalidException(TOKEN_VALIDATION_FAILED.getMessage());
            }

            // Check account status for active sessions
            UserDto userDto = userService.getUserByEmail(username);
            validateAccountStatus(userDto);

            return new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
        }
        return null;
    }

    private void validateAccountStatus(UserDto userDto) {
        if (!userDto.isEnabled()) {
            throw new CustomAccountStatusException(ACCOUNT_IS_DISABLED.getMessage());
        }

        if (userDto.isLocked() ||
            (userDto.getLockedUntil() != null && userDto.getLockedUntil().isAfter(LocalDateTime.now()))) {
            throw new CustomAccountStatusException(ACCOUNT_IS_LOCKED.getMessage());
        }

        if (userDto.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new CustomAccountStatusException(ACCOUNT_IS_NOT_ACTIVE.getMessage());
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message, int code, String path)
            throws IOException {
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        response.setStatus(code);
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                message,
                code,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                path
        );
        objectMapper.writeValue(response.getOutputStream(), Map.of(ERROR, errorResponse));
    }
}
