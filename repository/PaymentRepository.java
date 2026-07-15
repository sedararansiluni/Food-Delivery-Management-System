package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.model.Payment;
import com.example.foodorderingsystem.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order o JOIN FETCH o.user ORDER BY p.createdAt DESC")
    List<Payment> findAllWithOrderAndUser();

    @Query("SELECT p FROM Payment p JOIN FETCH p.order o WHERE o.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdWithOrder(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithOrder(@Param("paymentId") Long paymentId);
}