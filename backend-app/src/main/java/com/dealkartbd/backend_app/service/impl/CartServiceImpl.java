package com.dealkartbd.backend_app.service.impl;

import com.dealkartbd.backend_app.dto.cart.AddToCartRequest;
import com.dealkartbd.backend_app.dto.cart.CartResponse;
import com.dealkartbd.backend_app.dto.orders.CreateOrderRequest;
import com.dealkartbd.backend_app.entity.*;
import com.dealkartbd.backend_app.exception.ResourceNotFoundException;
import com.dealkartbd.backend_app.repository.*;
import com.dealkartbd.backend_app.service.CartService;
import com.dealkartbd.backend_app.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    @Override
    public CartResponse addToCart(AddToCartRequest request, Principal principal) {
        User user = getUserFromPrincipal(principal);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Validate minimum order quantity
        if (request.getQuantity() < product.getMinOrderQuantity()) {
            throw new IllegalArgumentException(
                String.format("Minimum order quantity for product %s is %d",
                product.getName(), product.getMinOrderQuantity()));
        }

        Cart cart = getOrCreateCart(user);

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addCartItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        log.info("Product {} added to cart for user: {}", product.getName(), user.getEmail());
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Principal principal) {
        User user = getUserFromPrincipal(principal);
        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElse(getOrCreateCart(user));

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItemQuantity(Long productId, Integer quantity, Principal principal) {
        User user = getUserFromPrincipal(principal);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (quantity < product.getMinOrderQuantity()) {
            throw new IllegalArgumentException(
                String.format("Minimum order quantity for product %s is %d",
                product.getName(), product.getMinOrderQuantity()));
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        log.info("Cart item quantity updated for product {} by user: {}", product.getName(), user.getEmail());

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(Long productId, Principal principal) {
        User user = getUserFromPrincipal(principal);
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        Cart cart = cartItem.getCart();
        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);

        log.info("Product {} removed from cart for user: {}", cartItem.getProduct().getName(), user.getEmail());
        return mapToCartResponse(cart);
    }

    @Override
    public void clearCart(Principal principal) {
        User user = getUserFromPrincipal(principal);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.clearCart();
        cartItemRepository.deleteByCartId(cart.getId());

        log.info("Cart cleared for user: {}", user.getEmail());
    }

    @Override
    public CartResponse createOrderFromCart(Long sellerCompanyId, String shippingAddress, String notes, Principal principal) {
        User user = getUserFromPrincipal(principal);
        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Validate all items belong to the same company
        Long productCompanyId = cart.getCartItems().get(0).getProduct().getCompany().getId();
        boolean allFromSameCompany = cart.getCartItems().stream()
                .allMatch(item -> item.getProduct().getCompany().getId().equals(productCompanyId));

        if (!allFromSameCompany || !productCompanyId.equals(sellerCompanyId)) {
            throw new IllegalArgumentException("All items in cart must be from the same company");
        }

        // Create order request from cart items
        List<CreateOrderRequest.OrderItemRequest> orderItems = cart.getCartItems().stream()
                .map(cartItem -> CreateOrderRequest.OrderItemRequest.builder()
                        .productId(cartItem.getProduct().getId())
                        .quantity(cartItem.getQuantity())
                        .build())
                .collect(Collectors.toList());

        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .sellerCompanyId(sellerCompanyId)
                .orderItems(orderItems)
                .shippingAddress(shippingAddress)
                .notes(notes)
                .build();

        // Create order
        orderService.createOrder(orderRequest, principal);

        // Clear cart after successful order creation
        clearCart(principal);

        log.info("Order created from cart for user: {}", user.getEmail());
        return mapToCartResponse(cart);
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartResponse.CartItemResponse> cartItems = cart.getCartItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = cartItems.stream()
                .map(CartResponse.CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = cartItems.stream()
                .mapToInt(CartResponse.CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .cartItems(cartItems)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartResponse.CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal totalPrice = BigDecimal.valueOf(product.getPrice())
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartResponse.CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .productPrice(BigDecimal.valueOf(product.getPrice()))
                .quantity(cartItem.getQuantity())
                .totalPrice(totalPrice)
                .companyName(product.getCompany().getName())
                .addedAt(cartItem.getCreatedAt())
                .build();
    }
}
