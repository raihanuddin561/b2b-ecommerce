package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.cart.AddToCartRequest;
import com.dealkartbd.backend_app.dto.cart.CartResponse;
import com.dealkartbd.backend_app.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Principal principal) {
        CartResponse response = cartService.addToCart(request, principal);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        CartResponse response = cartService.getCart(principal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{productId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            Principal principal) {
        CartResponse response = cartService.updateCartItemQuantity(productId, quantity, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long productId,
            Principal principal) {
        CartResponse response = cartService.removeFromCart(productId, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<String> clearCart(Principal principal) {
        cartService.clearCart(principal);
        return ResponseEntity.ok("Cart cleared successfully");
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<CartResponse> createOrderFromCart(
            @RequestParam Long sellerCompanyId,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String notes,
            Principal principal) {
        CartResponse response = cartService.createOrderFromCart(sellerCompanyId, shippingAddress, notes, principal);
        return ResponseEntity.ok(response);
    }
}
