package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.UserDto;
import com.dealkartbd.backend_app.exception.InvalidPathException;
import com.dealkartbd.backend_app.exception.ResourceNotFoundException;
import com.dealkartbd.backend_app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserDto> getProfile(@PathVariable @NotNull @Min(1) Long userId) {
        log.info("Fetching profile for userId: {}", userId);

        // Validate userId format and range
        validateUserId(userId);

        try {
            // Since getUserById might not exist, let's use getUserByEmail for now
            // You'll need to implement getUserById in UserService
            UserDto userDto = userService.getUserByEmail("placeholder@email.com"); // TODO: implement getUserById
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            log.error("Error fetching profile for userId: {}", userId, e);
            throw new ResourceNotFoundException("User", "ID", userId.toString());
        }
    }

    @GetMapping("/{userId}/settings")
    public ResponseEntity<String> getUserSettings(@PathVariable @NotNull @Min(1) Long userId) {
        log.info("Fetching settings for userId: {}", userId);

        validateUserId(userId);

        // TODO: Implement user settings logic
        return ResponseEntity.ok("User settings for userId: " + userId);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<String> updateProfile(@PathVariable @NotNull @Min(1) Long userId,
                                               @RequestBody @NotNull Object updateRequest) {
        log.info("Updating profile for userId: {}", userId);

        validateUserId(userId);

        // TODO: Implement profile update logic
        return ResponseEntity.ok("Profile updated successfully for userId: " + userId);
    }

    /**
     * Validates userId to ensure it's within acceptable range and format
     * Throws professional exceptions for wrong path scenarios
     */
    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new InvalidPathException("User ID cannot be null");
        }

        if (userId <= 0) {
            throw new InvalidPathException("User ID must be a positive number, received: " + userId);
        }

        // Add business logic validation - e.g., reasonable upper limit
        if (userId > 999999999L) { // 9 digits max
            throw new InvalidPathException("User ID exceeds maximum allowed value: " + userId);
        }
    }

    /**
     * Catch-all handler for any unmatched sub-paths under /api/user/{userId}/*
     * This provides professional error handling for wrong paths
     */
    @RequestMapping("/{userId}/**")
    public ResponseEntity<String> handleInvalidSubPath(@PathVariable Long userId,
                                                      HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        log.warn("Invalid path accessed: {} {}", method, requestPath);

        throw new InvalidPathException(requestPath,
            String.format("Invalid endpoint. Available endpoints for user %d: /profile, /settings", userId));
    }
}
