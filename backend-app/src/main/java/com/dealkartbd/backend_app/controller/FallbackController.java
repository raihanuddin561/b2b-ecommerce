package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.exception.EndpointNotFoundException;
import com.dealkartbd.backend_app.exception.ServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback controller to handle all unmatched API paths
 * This provides professional error responses for wrong paths
 */
@RestController
@RequestMapping("/api")
public class FallbackController {

    /**
     * Catch-all handler for any unmatched paths under /api/**
     * This ensures professional error handling for all wrong API paths
     */
    @RequestMapping("/**")
    public ResponseEntity<Map<String, String>> handleUnmatchedPaths(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Determine appropriate exception based on path pattern
        if (path.contains("/admin") || path.contains("/management")) {
            throw ServiceUnavailableException.forWrongPath(path);
        } else if (path.matches(".*\\d+.*") && !isValidNumericPath(path)) {
            throw new EndpointNotFoundException(method, path);
        } else {
            throw new EndpointNotFoundException(
                String.format("API endpoint '%s %s' does not exist. Please check the API documentation for valid endpoints.",
                method, path)
            );
        }
    }

    /**
     * Validates if numeric values in path are reasonable
     */
    private boolean isValidNumericPath(String path) {
        // Extract numbers from path and validate they're reasonable
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                try {
                    long num = Long.parseLong(part);
                    if (num <= 0 || num > 999999999L) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }
}
