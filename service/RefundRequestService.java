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
public class RefundRequestService {

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    public RefundRequest createRefundRequest(Long paymentId, Long userId, String customerName,
                                             String contactEmail, String contactPhone, String bankName,
                                             String accountHolderName, String accountNumber,
                                             String bankBranch, String refundReason) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if payment belongs to user
        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        // Check if payment is rejected
        if (payment.getPaymentStatus() != PaymentStatus.REJECTED) {
            throw new RuntimeException("Refund request can only be made for rejected payments");
        }

        // Check if refund request already exists
        Optional<RefundRequest> existingRequest = refundRequestRepository.findByPaymentId(paymentId);
        if (existingRequest.isPresent()) {
            throw new RuntimeException("Refund request already exists for this payment");
        }

        String requestNumber = generateRequestNumber();
        RefundRequest refundRequest = new RefundRequest(payment, user, requestNumber);
        refundRequest.setCustomerName(customerName);
        refundRequest.setContactEmail(contactEmail);
        refundRequest.setContactPhone(contactPhone);
        refundRequest.setBankName(bankName);
        refundRequest.setAccountHolderName(accountHolderName);
        refundRequest.setAccountNumber(accountNumber);
        refundRequest.setBankBranch(bankBranch);
        refundRequest.setRefundReason(refundReason);

        return refundRequestRepository.save(refundRequest);
    }

    @Transactional(readOnly = true)
    public RefundRequest getRefundRequestById(Long refundId) {
        return refundRequestRepository.findByIdWithDetails(refundId)
                .orElseThrow(() -> new RuntimeException("Refund request not found"));
    }

    @Transactional(readOnly = true)
    public RefundRequest getRefundRequestByPaymentId(Long paymentId) {
        return refundRequestRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Refund request not found"));
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getAllRefundRequests() {
        return refundRequestRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getUserRefundRequests(Long userId) {
        return refundRequestRepository.findByUserIdWithDetails(userId);
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getRefundRequestsByStatus(RefundStatus status) {
        return refundRequestRepository.findByStatus(status);
    }

    public RefundRequest approveRefundRequest(Long refundId, String processedBy, String adminNotes) {
        RefundRequest refundRequest = getRefundRequestById(refundId);

        if (refundRequest.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Refund request is not in pending status");
        }

        refundRequest.setStatus(RefundStatus.APPROVED);
        refundRequest.setProcessedAt(LocalDateTime.now());
        refundRequest.setProcessedBy(processedBy);
        if (adminNotes != null && !adminNotes.isEmpty()) {
            refundRequest.setAdminNotes(adminNotes);
        }

        return refundRequestRepository.save(refundRequest);
    }

    public RefundRequest rejectRefundRequest(Long refundId, String processedBy, String adminNotes) {
        RefundRequest refundRequest = getRefundRequestById(refundId);

        if (refundRequest.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Refund request is not in pending status");
        }

        refundRequest.setStatus(RefundStatus.REJECTED);
        refundRequest.setProcessedAt(LocalDateTime.now());
        refundRequest.setProcessedBy(processedBy);
        refundRequest.setAdminNotes(adminNotes);

        return refundRequestRepository.save(refundRequest);
    }

    public RefundRequest completeRefundRequest(Long refundId, String processedBy, String adminNotes) {
        RefundRequest refundRequest = getRefundRequestById(refundId);

        if (refundRequest.getStatus() != RefundStatus.APPROVED) {
            throw new RuntimeException("Only approved refund requests can be marked as completed");
        }

        refundRequest.setStatus(RefundStatus.COMPLETED);
        refundRequest.setProcessedAt(LocalDateTime.now());
        refundRequest.setProcessedBy(processedBy);
        if (adminNotes != null && !adminNotes.isEmpty()) {
            refundRequest.setAdminNotes(adminNotes);
        }

        // Update payment status to REFUNDED
        Payment payment = refundRequest.getPayment();
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return refundRequestRepository.save(refundRequest);
    }

    private String generateRequestNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "REF-" + timestamp + "-" + (int)(Math.random() * 10000);
    }
}