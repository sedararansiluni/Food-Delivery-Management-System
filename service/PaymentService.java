package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.model.*;
import com.example.foodorderingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public Payment createPayment(Long orderId, String paymentMethod, String cardHolderName,
                                 String cardNumber, String cvv, String expiryDate) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if payment already exists for this order
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment already exists for this order");
        }

        Payment payment = new Payment(order, paymentMethod, order.getTotalAmount());

        if ("CARD".equals(paymentMethod)) {
            payment.setCardHolderName(cardHolderName);
            // Store only last 4 digits for security
            payment.setCardNumber("****" + cardNumber.substring(cardNumber.length() - 4));
        }

        // Generate transaction ID
        payment.setTransactionId(generateTransactionId());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findByIdWithOrder(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for this order"));
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAllWithOrderAndUser();
    }

    @Transactional(readOnly = true)
    public List<Payment> getUserPayments(Long userId) {
        return paymentRepository.findByUserIdWithOrder(userId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    public Payment approvePayment(Long paymentId, String approvedBy, String notes) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status");
        }

        payment.setPaymentStatus(PaymentStatus.APPROVED);
        payment.setApprovedAt(LocalDateTime.now());
        payment.setApprovedBy(approvedBy);
        if (notes != null && !notes.isEmpty()) {
            payment.setPaymentNotes(notes);
        }

        // Update order status to CONFIRMED when payment is approved
        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        return paymentRepository.save(payment);
    }

    public Payment rejectPayment(Long paymentId, String rejectedBy, String notes) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status");
        }

        payment.setPaymentStatus(PaymentStatus.REJECTED);
        payment.setApprovedBy(rejectedBy);
        payment.setPaymentNotes(notes);

        // Cancel the order when payment is rejected
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return paymentRepository.save(payment);
    }

    public Payment refundPayment(Long paymentId, String refundedBy, String notes) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getPaymentStatus() != PaymentStatus.APPROVED) {
            throw new RuntimeException("Only approved payments can be refunded");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setPaymentNotes(notes);

        return paymentRepository.save(payment);
    }

    private String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "TXN-" + timestamp + "-" + (int)(Math.random() * 10000);
    }
}