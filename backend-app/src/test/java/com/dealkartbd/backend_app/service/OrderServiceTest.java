package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.orders.CreateOrderRequest;
import com.dealkartbd.backend_app.dto.orders.OrderResponse;
import com.dealkartbd.backend_app.entity.*;
import com.dealkartbd.backend_app.exception.ResourceNotFoundException;
import com.dealkartbd.backend_app.repository.*;
import com.dealkartbd.backend_app.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private Principal principal;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testBuyer;
    private Company testCompany;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCompany = Company.builder().build();
        testCompany.setId(1L);
        testCompany.setName("Test Company");

        testBuyer = User.builder().build();
        testBuyer.setId(1L);
        testBuyer.setEmail("buyer@test.com");
        testBuyer.setFullName("John Doe");

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(100.0)
                .minOrderQuantity(1)
                .company(testCompany)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-123456")
                .buyer(testBuyer)
                .sellerCompany(testCompany)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.0))
                .subtotal(BigDecimal.valueOf(100.0))
                .taxAmount(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .build();
    }

    @Test
    void createOrder_ValidRequest_ShouldCreateOrderSuccessfully() {
        // Arrange
        CreateOrderRequest.OrderItemRequest itemRequest = CreateOrderRequest.OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .sellerCompanyId(1L)
                .orderItems(Arrays.asList(itemRequest))
                .shippingAddress("Test Address")
                .notes("Test Notes")
                .build();

        when(principal.getName()).thenReturn("buyer@test.com");
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(testBuyer));
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.createOrder(request, principal);

        // Assert
        assertNotNull(response);
        assertEquals("ORD-123456", response.getOrderNumber());
        assertEquals("John Doe", response.getBuyerName());
        assertEquals("buyer@test.com", response.getBuyerEmail());
        assertEquals("Test Company", response.getSellerCompanyName());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        CreateOrderRequest.OrderItemRequest itemRequest = CreateOrderRequest.OrderItemRequest.builder()
                .productId(999L)
                .quantity(2)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .sellerCompanyId(1L)
                .orderItems(Arrays.asList(itemRequest))
                .build();

        when(principal.getName()).thenReturn("buyer@test.com");
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(testBuyer));
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(request, principal);
        });
    }

    @Test
    void createOrder_QuantityBelowMinimum_ShouldThrowIllegalArgumentException() {
        // Arrange
        testProduct.setMinOrderQuantity(5);

        CreateOrderRequest.OrderItemRequest itemRequest = CreateOrderRequest.OrderItemRequest.builder()
                .productId(1L)
                .quantity(2) // Below minimum of 5
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .sellerCompanyId(1L)
                .orderItems(Arrays.asList(itemRequest))
                .build();

        when(principal.getName()).thenReturn("buyer@test.com");
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(testBuyer));
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request, principal);
        });
    }

    @Test
    void getOrderById_ValidOrderAndUser_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(principal.getName()).thenReturn("buyer@test.com");
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(testBuyer));

        // Act
        OrderResponse response = orderService.getOrderById(1L, principal);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ORD-123456", response.getOrderNumber());
    }

    @Test
    void getOrderById_OrderNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999L, principal);
        });
    }

    @Test
    void getOrderById_UnauthorizedUser_ShouldThrowAccessDeniedException() {
        // Arrange
        User unauthorizedUser = User.builder().build();
        unauthorizedUser.setId(999L);
        unauthorizedUser.setEmail("unauthorized@test.com");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(principal.getName()).thenReturn("unauthorized@test.com");
        when(userRepository.findByEmail("unauthorized@test.com")).thenReturn(Optional.of(unauthorizedUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            orderService.getOrderById(1L, principal);
        });
    }

    @Test
    void updateOrderStatus_ValidRequest_ShouldUpdateStatus() {
        // Arrange
        User companyOwner = User.builder().build();
        companyOwner.setId(2L);
        companyOwner.setEmail("owner@test.com");
        companyOwner.setCompany(testCompany);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(principal.getName()).thenReturn("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(companyOwner));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED, principal);

        // Assert
        assertNotNull(response);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_ValidPendingOrder_ShouldCancelSuccessfully() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(principal.getName()).thenReturn("buyer@test.com");
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(testBuyer));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.cancelOrder(1L, principal);

        // Assert
        assertNotNull(response);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_ShippedOrder_ShouldThrowIllegalStateException() {
        // Arrange
        testOrder.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(principal.getName()).thenReturn("buyer@test.com");
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(testBuyer));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(1L, principal);
        });
    }

    @Test
    void getMyOrders_ValidBuyer_ShouldReturnOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);

        when(principal.getName()).thenReturn("buyer@test.com");
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(testBuyer));
        when(orderRepository.findByBuyerId(1L)).thenReturn(orders);

        // Act
        List<OrderResponse> responses = orderService.getMyOrders(principal);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("ORD-123456", responses.get(0).getOrderNumber());
    }
}
