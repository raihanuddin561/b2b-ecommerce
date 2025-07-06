package com.dealkartbd.backend_app.security;

import com.dealkartbd.backend_app.dto.UserDto;
import com.dealkartbd.backend_app.dto.login.LoginRequest;
import com.dealkartbd.backend_app.dto.login.LoginResponse;
import com.dealkartbd.backend_app.entity.AccountStatus;
import com.dealkartbd.backend_app.exception.ErrorMessage;
import com.dealkartbd.backend_app.service.UserService;
import com.dealkartbd.backend_app.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.dealkartbd.backend_app.constans.AppConstants.ERROR;
import static com.dealkartbd.backend_app.exception.ErrorMessages.*;
import static com.dealkartbd.backend_app.security.SecurityConstants.*;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, Environment env,
                                      UserService userService) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        setFilterProcessesUrl(env.getProperty(LOGIN_PATH));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

            // Check account status before authentication
            UserDto userDto = userService.getUserByEmail(loginRequest.email());
            validateAccountStatus(userDto);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    );
            setDetails(request, authenticationToken);
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException | AuthenticationException e) {
            catchActionForAuthenticationException(response, e, HttpServletResponse.SC_UNAUTHORIZED);
        }
        return null;
    }

    private void validateAccountStatus(UserDto userDto) throws AuthenticationException {
        if (!userDto.isEnabled()) {
            throw new DisabledException(ACCOUNT_IS_DISABLED.getMessage());
        }

        if (userDto.isLocked() ||
                (userDto.getLockedUntil() != null && userDto.getLockedUntil().isAfter(LocalDateTime.now()))) {
            throw new LockedException(ACCOUNT_IS_LOCKED.getMessage());
        }

        if (userDto.getAccountStatus() == AccountStatus.PENDING_ACTIVATION) {
            throw new DisabledException(CONFIRM_YOUR_EMAIL_ADDRESS_MSG);
        }

        if (userDto.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new DisabledException(ACCOUNT_IS_SUSPENDED.getMessage());
        }

        if (userDto.getAccountStatus() == AccountStatus.DEACTIVATED) {
            throw new DisabledException(ACCOUNT_IS_DEACTIVATED.getMessage());
        }

        if (userDto.getAccountStatus() == AccountStatus.BANNED) {
            throw new DisabledException(ACCOUNT_IS_BANNED.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        String username = authResult.getName();
        UserDto userDto = userService.getUserByEmail(username);

        // Update last login and reset failed attempts
        userService.updateLastLogin(username);

        List<String> roles = userDto.getRoles().stream().toList();
        String token = jwtUtil.generateToken(username, authResult.getAuthorities());
        LoginResponse loginResponse = new LoginResponse(token, roles, userDto.getUserType().name());
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        response.setCharacterEncoding(CHAR_SET_UTF_8);
        new ObjectMapper().writeValue(response.getOutputStream(), loginResponse);
    }

    @Override
    protected AuthenticationFailureHandler getFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
            response.setCharacterEncoding(CHAR_SET_UTF_8);
            response.getWriter().write("{\"error\": " + new ErrorMessage(HttpServletResponse.SC_UNAUTHORIZED,
                    exception.getMessage()) + "\"}");
        };
    }

    private void catchActionForAuthenticationException(HttpServletResponse response,
                                                       Exception e, int errorCode) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ErrorMessage errorMessage = new ErrorMessage(errorCode, e.getMessage());
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        try {
            new ObjectMapper().writeValue(response.getOutputStream(), Map.of(ERROR, errorMessage));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
