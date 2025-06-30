package com.dealkartbd.backend_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @PostMapping("/profile")
    public ResponseEntity<String> getProfile(){
        return ResponseEntity.ok("User profile information");
    }
}
