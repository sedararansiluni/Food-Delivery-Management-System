package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.model.Payment;
import com.example.foodorderingsystem.model.RefundRequest;
import com.example.foodorderingsystem.model.User;
import com.example.foodorderingsystem.service.PaymentService;
import com.example.foodorderingsystem.service.RefundRequestService;
import com.example.foodorderingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/refunds")
public class RefundRequestController {

    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    // Show refund request form
    @GetMapping("/request/{paymentId}")
    public String showRefundRequestForm(@PathVariable Long paymentId,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to continue");
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            Payment payment = paymentService.getPaymentById(paymentId);

            // Check if payment belongs to user
            if (!payment.getOrder().getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/payments/my";
            }

            // Check if refund request already exists
            try {
                RefundRequest existingRequest = refundRequestService.getRefundRequestByPaymentId(paymentId);
                redirectAttributes.addFlashAttribute("message", "Refund request already submitted");
                return "redirect:/refunds/" + existingRequest.getId();
            } catch (RuntimeException e) {
                // No existing request, show form
            }

            model.addAttribute("payment", payment);
            model.addAttribute("user", user);
            return "refund-request-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/payments/my";
        }
    }

    // Submit refund request
    @PostMapping("/submit")
    public String submitRefundRequest(@RequestParam Long paymentId,
                                      @RequestParam String customerName,
                                      @RequestParam String contactEmail,
                                      @RequestParam String contactPhone,
                                      @RequestParam String bankName,
                                      @RequestParam String accountHolderName,
                                      @RequestParam String accountNumber,
                                      @RequestParam(required = false) String bankBranch,
                                      @RequestParam String refundReason,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            RefundRequest refundRequest = refundRequestService.createRefundRequest(
                    paymentId, user.getId(), customerName, contactEmail, contactPhone,
                    bankName, accountHolderName, accountNumber, bankBranch, refundReason
            );
            redirectAttributes.addFlashAttribute("message",
                    "Refund request submitted successfully! Request Number: " + refundRequest.getRequestNumber());
            return "redirect:/refunds/" + refundRequest.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/refunds/request/" + paymentId;
        }
    }

    // View refund request details
    @GetMapping("/{refundId}")
    public String viewRefundRequest(@PathVariable Long refundId,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        if (email == null) {
            return "redirect:/login";
        }

        try {
            RefundRequest refundRequest = refundRequestService.getRefundRequestById(refundId);

            // Check authorization
            if (role != null && role.equals("ADMIN")) {
                model.addAttribute("isAdmin", true);
            } else {
                User user = userService.findByEmail(email);
                if (!refundRequest.getUser().getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                    return "redirect:/refunds/my";
                }
                model.addAttribute("isAdmin", false);
            }

            model.addAttribute("refundRequest", refundRequest);
            return "refund-request-view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return role != null && role.equals("ADMIN") ?
                    "redirect:/refunds/admin/all" : "redirect:/refunds/my";
        }
    }

    // List user's refund requests
    @GetMapping("/my")
    public String listMyRefundRequests(HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view refund requests");
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            List<RefundRequest> refundRequests = refundRequestService.getUserRefundRequests(user.getId());
            model.addAttribute("refundRequests", refundRequests);
            return "refund-request-list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    // Admin: List all refund requests
    @GetMapping("/admin/all")
    public String listAllRefundRequests(HttpSession session,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        List<RefundRequest> refundRequests = refundRequestService.getAllRefundRequests();
        model.addAttribute("refundRequests", refundRequests);
        return "refund-request-admin-list";
    }

    // Admin: Approve refund request
    @PostMapping("/admin/{refundId}/approve")
    public String approveRefundRequest(@PathVariable Long refundId,
                                       @RequestParam(required = false) String adminNotes,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        try {
            refundRequestService.approveRefundRequest(refundId, email, adminNotes);
            redirectAttributes.addFlashAttribute("message", "Refund request approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/refunds/admin/all";
    }

    // Admin: Reject refund request
    @PostMapping("/admin/{refundId}/reject")
    public String rejectRefundRequest(@PathVariable Long refundId,
                                      @RequestParam String adminNotes,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        try {
            refundRequestService.rejectRefundRequest(refundId, email, adminNotes);
            redirectAttributes.addFlashAttribute("message", "Refund request rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/refunds/admin/all";
    }

    // Admin: Mark refund as completed
    @PostMapping("/admin/{refundId}/complete")
    public String completeRefundRequest(@PathVariable Long refundId,
                                        @RequestParam(required = false) String adminNotes,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        try {
            refundRequestService.completeRefundRequest(refundId, email, adminNotes);
            redirectAttributes.addFlashAttribute("message", "Refund marked as completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/refunds/admin/all";
    }
}