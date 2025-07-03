package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.products.ProductRequest;
import com.dealkartbd.backend_app.dto.products.ProductResponse;
import com.dealkartbd.backend_app.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request, Principal principal) {
        productService.addProduct(request, principal);
        return ResponseEntity.ok("Product added successfully");
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.search(q));
    }
}
