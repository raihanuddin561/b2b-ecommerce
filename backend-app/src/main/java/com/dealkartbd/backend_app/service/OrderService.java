package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.orders.CreateOrderRequest;
import com.dealkartbd.backend_app.dto.orders.OrderResponse;
import com.dealkartbd.backend_app.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, Principal principal);

    OrderResponse getOrderById(Long orderId, Principal principal);

    OrderResponse getOrderByOrderNumber(String orderNumber, Principal principal);

    List<OrderResponse> getMyOrders(Principal principal);

    Page<OrderResponse> getMyOrders(Principal principal, Pageable pageable);

    List<OrderResponse> getCompanyOrders(Principal principal);

    Page<OrderResponse> getCompanyOrders(Principal principal, Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status, Principal principal);

    OrderResponse cancelOrder(Long orderId, Principal principal);

    List<OrderResponse> getOrdersByStatus(OrderStatus status, Principal principal);

    // Admin methods
    List<OrderResponse> getAllOrders();

    Page<OrderResponse> getAllOrders(Pageable pageable);
}
