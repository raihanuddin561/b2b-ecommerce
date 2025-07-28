package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.orders.CreateOrderRequest;
import com.dealkartbd.backend_app.dto.orders.OrderResponse;
import com.dealkartbd.backend_app.entity.OrderStatus;
import com.dealkartbd.backend_app.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'COMPANY_OWNER')")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal) {
        OrderResponse response = orderService.createOrder(request, principal);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('BUYER', 'COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Principal principal) {
        OrderResponse response = orderService.getOrderById(orderId, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order-number/{orderNumber}")
    @PreAuthorize("hasAnyRole('BUYER', 'COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            Principal principal) {
        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal) {
        List<OrderResponse> responses = orderService.getMyOrders(principal);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-orders/paginated")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<Page<OrderResponse>> getMyOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Principal principal) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> responses = orderService.getMyOrders(principal, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/company-orders")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getCompanyOrders(Principal principal) {
        List<OrderResponse> responses = orderService.getCompanyOrders(principal);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/company-orders/paginated")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getCompanyOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Principal principal) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> responses = orderService.getCompanyOrders(principal, pageable);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Principal principal) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status, principal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('BUYER', 'COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            Principal principal) {
        OrderResponse response = orderService.cancelOrder(orderId, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('BUYER', 'COMPANY_OWNER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Principal principal) {
        List<OrderResponse> responses = orderService.getOrdersByStatus(status, principal);
        return ResponseEntity.ok(responses);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderService.getAllOrders();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/all/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> responses = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(responses);
    }
}
