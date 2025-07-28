package com.dealkartbd.backend_app.repository;

import com.dealkartbd.backend_app.entity.Order;
import com.dealkartbd.backend_app.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByBuyerId(Long buyerId);

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    List<Order> findBySellerCompanyId(Long companyId);

    Page<Order> findBySellerCompanyId(Long companyId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.buyer.id = :buyerId AND o.status = :status")
    List<Order> findByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.sellerCompany.id = :companyId AND o.status = :status")
    List<Order> findBySellerCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyer.id = :buyerId")
    Long countOrdersByBuyerId(@Param("buyerId") Long buyerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerCompany.id = :companyId")
    Long countOrdersByCompanyId(@Param("companyId") Long companyId);
}
