package com.dealkartbd.backend_app.service.impl;

import com.dealkartbd.backend_app.dto.orders.CreateOrderRequest;
import com.dealkartbd.backend_app.dto.orders.OrderResponse;
import com.dealkartbd.backend_app.entity.*;
import com.dealkartbd.backend_app.exception.ResourceNotFoundException;
import com.dealkartbd.backend_app.repository.*;
import com.dealkartbd.backend_app.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, Principal principal) {
        User buyer = getUserFromPrincipal(principal);
        Company sellerCompany = companyRepository.findById(request.getSellerCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .buyer(buyer)
                .sellerCompany(sellerCompany)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .shippingCost(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .build();

        // Add order items
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Validate minimum order quantity
            if (itemRequest.getQuantity() < product.getMinOrderQuantity()) {
                throw new IllegalArgumentException(
                    String.format("Minimum order quantity for product %s is %d",
                    product.getName(), product.getMinOrderQuantity()));
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(BigDecimal.valueOf(product.getPrice()))
                    .build();

            orderItem.calculateTotalPrice();
            order.addOrderItem(orderItem);
        }

        // Calculate total amount
        order.calculateTotalAmount();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {} for user: {}", savedOrder.getId(), buyer.getEmail());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Principal principal) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateOrderAccess(order, principal);
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber, Principal principal) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateOrderAccess(order, principal);
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Principal principal) {
        User user = getUserFromPrincipal(principal);
        List<Order> orders = orderRepository.findByBuyerId(user.getId());
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Principal principal, Pageable pageable) {
        User user = getUserFromPrincipal(principal);
        Page<Order> orders = orderRepository.findByBuyerId(user.getId(), pageable);
        return orders.map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getCompanyOrders(Principal principal) {
        User user = getUserFromPrincipal(principal);
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("User is not associated with any company");
        }

        List<Order> orders = orderRepository.findBySellerCompanyId(company.getId());
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCompanyOrders(Principal principal, Pageable pageable) {
        User user = getUserFromPrincipal(principal);
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("User is not associated with any company");
        }

        Page<Order> orders = orderRepository.findBySellerCompanyId(company.getId(), pageable);
        return orders.map(this::mapToOrderResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, Principal principal) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User user = getUserFromPrincipal(principal);

        // Only seller company can update order status (except cancellation)
        if (!order.getSellerCompany().getId().equals(user.getCompany().getId()) && status != OrderStatus.CANCELLED) {
            throw new AccessDeniedException("You don't have permission to update this order");
        }

        order.setStatus(status);

        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        } else if (status == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} status updated to {} by user: {}", orderId, status, user.getEmail());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Principal principal) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateOrderAccess(order, principal);

        // Can only cancel pending or confirmed orders
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} cancelled by user: {}", orderId, principal.getName());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status, Principal principal) {
        User user = getUserFromPrincipal(principal);
        List<Order> orders;

        if (user.getCompany() != null) {
            orders = orderRepository.findBySellerCompanyIdAndStatus(user.getCompany().getId(), status);
        } else {
            orders = orderRepository.findByBuyerIdAndStatus(user.getId(), status);
        }

        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::mapToOrderResponse);
    }

    private User getUserFromPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateOrderAccess(Order order, Principal principal) {
        User user = getUserFromPrincipal(principal);

        boolean isBuyer = order.getBuyer().getId().equals(user.getId());
        boolean isSeller = user.getCompany() != null &&
                          order.getSellerCompany().getId().equals(user.getCompany().getId());

        if (!isBuyer && !isSeller) {
            throw new AccessDeniedException("You don't have permission to access this order");
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .buyerName(order.getBuyer().getFullName())
                .buyerEmail(order.getBuyer().getEmail())
                .sellerCompanyName(order.getSellerCompany().getName())
                .orderItems(orderItems)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingCost(order.getShippingCost())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    private OrderResponse.OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderResponse.OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .productImageUrl(orderItem.getProduct().getImageUrl())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}
