package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.cart.AddToCartRequest;
import com.dealkartbd.backend_app.dto.cart.CartResponse;

import java.security.Principal;

public interface CartService {

    CartResponse addToCart(AddToCartRequest request, Principal principal);

    CartResponse getCart(Principal principal);

    CartResponse updateCartItemQuantity(Long productId, Integer quantity, Principal principal);

    CartResponse removeFromCart(Long productId, Principal principal);

    void clearCart(Principal principal);

    CartResponse createOrderFromCart(Long sellerCompanyId, String shippingAddress, String notes, Principal principal);
}
