package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.cart.AddToCartRequest;
import com.dealkartbd.backend_app.dto.cart.CartResponse;
import com.dealkartbd.backend_app.entity.*;
import com.dealkartbd.backend_app.exception.ResourceNotFoundException;
import com.dealkartbd.backend_app.repository.*;
import com.dealkartbd.backend_app.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private Principal principal;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Company testCompany;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(1L)
                .name("Test Company")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("user@test.com")
                .fullName("John Doe")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .minOrderQuantity(1)
                .imageUrl("http://test.com/image.jpg")
                .company(testCompany)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();

        testCart.setCartItems(Arrays.asList(testCartItem));
    }

    @Test
    void addToCart_ValidRequest_ShouldAddProductToCart() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.addToCart(request, principal);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_ProductNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(999L)
                .quantity(2)
                .build();

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart(request, principal);
        });
    }

    @Test
    void addToCart_QuantityBelowMinimum_ShouldThrowIllegalArgumentException() {
        // Arrange
        testProduct.setMinOrderQuantity(5);

        AddToCartRequest request = AddToCartRequest.builder()
                .productId(1L)
                .quantity(2) // Below minimum of 5
                .build();

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.addToCart(request, principal);
        });
    }

    @Test
    void addToCart_ProductAlreadyInCart_ShouldUpdateQuantity() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(1L)
                .quantity(3)
                .build();

        CartItem existingCartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingCartItem);

        // Act
        CartResponse response = cartService.addToCart(request, principal);

        // Assert
        assertNotNull(response);
        assertEquals(5, existingCartItem.getQuantity()); // 2 + 3
        verify(cartItemRepository).save(existingCartItem);
    }

    @Test
    void getCart_ValidUser_ShouldReturnCart() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.getCart(principal);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(1, response.getCartItems().size());
    }

    @Test
    void getCart_NoExistingCart_ShouldCreateNewCart() {
        // Arrange
        Cart newCart = Cart.builder()
                .id(2L)
                .user(testUser)
                .build();

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        // Act
        CartResponse response = cartService.getCart(principal);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.getId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_ValidRequest_ShouldUpdateQuantity() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.updateCartItemQuantity(1L, 5, principal);

        // Assert
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    void updateCartItemQuantity_ItemNotInCart_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateCartItemQuantity(1L, 5, principal);
        });
    }

    @Test
    void removeFromCart_ValidItem_ShouldRemoveSuccessfully() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));

        // Act
        CartResponse response = cartService.removeFromCart(1L, principal);

        // Assert
        assertNotNull(response);
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void removeFromCart_ItemNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserIdAndProductId(1L, 999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(999L, principal);
        });
    }

    @Test
    void clearCart_ValidUser_ShouldClearCartSuccessfully() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        cartService.clearCart(principal);

        // Assert
        verify(cartItemRepository).deleteByCartId(1L);
    }

    @Test
    void clearCart_CartNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.clearCart(principal);
        });
    }

    @Test
    void createOrderFromCart_ValidCart_ShouldCreateOrderAndClearCart() {
        // Arrange
        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.createOrderFromCart(1L, "Test Address", "Test Notes", principal);

        // Assert
        assertNotNull(response);
        verify(orderService).createOrder(any(), eq(principal));
        verify(cartItemRepository).deleteByCartId(1L);
    }

    @Test
    void createOrderFromCart_EmptyCart_ShouldThrowIllegalStateException() {
        // Arrange
        Cart emptyCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .build();

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(emptyCart));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            cartService.createOrderFromCart(1L, "Test Address", "Test Notes", principal);
        });
    }

    @Test
    void createOrderFromCart_ItemsFromDifferentCompanies_ShouldThrowIllegalArgumentException() {
        // Arrange
        Company otherCompany = Company.builder()
                .id(2L)
                .name("Other Company")
                .build();

        Product otherProduct = Product.builder()
                .id(2L)
                .name("Other Product")
                .company(otherCompany)
                .build();

        CartItem otherCartItem = CartItem.builder()
                .id(2L)
                .cart(testCart)
                .product(otherProduct)
                .quantity(1)
                .build();

        testCart.setCartItems(Arrays.asList(testCartItem, otherCartItem));

        when(principal.getName()).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.createOrderFromCart(1L, "Test Address", "Test Notes", principal);
        });
    }
}
