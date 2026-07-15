package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.model.RefundRequest;
import com.example.foodorderingsystem.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    Optional<RefundRequest> findByPaymentId(Long paymentId);

    Optional<RefundRequest> findByRequestNumber(String requestNumber);

    List<RefundRequest> findByStatus(RefundStatus status);

    @Query("SELECT r FROM RefundRequest r JOIN FETCH r.payment p JOIN FETCH p.order o JOIN FETCH o.user WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<RefundRequest> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT r FROM RefundRequest r JOIN FETCH r.payment p JOIN FETCH p.order o JOIN FETCH o.user ORDER BY r.createdAt DESC")
    List<RefundRequest> findAllWithDetails();

    @Query("SELECT r FROM RefundRequest r JOIN FETCH r.payment p JOIN FETCH p.order WHERE r.id = :refundId")
    Optional<RefundRequest> findByIdWithDetails(@Param("refundId") Long refundId);
}