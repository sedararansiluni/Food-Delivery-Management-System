package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.model.Payment;
import com.example.foodorderingsystem.model.Order;
import com.example.foodorderingsystem.model.User;
import com.example.foodorderingsystem.service.PaymentService;
import com.example.foodorderingsystem.service.OrderService;
import com.example.foodorderingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // Show payment form for an order
    @GetMapping("/order/{orderId}")
    public String showPaymentForm(@PathVariable Long orderId,
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
            Order order = orderService.getOrderById(orderId);

            // Check if user owns this order
            if (!order.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/orders";
            }

            // Check if payment already exists
            try {
                Payment existingPayment = paymentService.getPaymentByOrderId(orderId);
                redirectAttributes.addFlashAttribute("message", "Payment already submitted for this order");
                return "redirect:/payments/" + existingPayment.getId();
            } catch (RuntimeException e) {
                // Payment doesn't exist, show form
            }

            model.addAttribute("order", order);
            return "payment-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders";
        }
    }

    // Process payment
    @PostMapping("/process")
    public String processPayment(@RequestParam Long orderId,
                                 @RequestParam String paymentMethod,
                                 @RequestParam(required = false) String cardHolderName,
                                 @RequestParam(required = false) String cardNumber,
                                 @RequestParam(required = false) String cvv,
                                 @RequestParam(required = false) String expiryDate,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            Payment payment = paymentService.createPayment(orderId, paymentMethod,
                    cardHolderName, cardNumber, cvv, expiryDate);
            redirectAttributes.addFlashAttribute("message",
                    "Payment submitted successfully! Transaction ID: " + payment.getTransactionId());
            return "redirect:/payments/" + payment.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/payments/order/" + orderId;
        }
    }

    // View payment details
    @GetMapping("/{paymentId}")
    public String viewPayment(@PathVariable Long paymentId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        if (email == null) {
            return "redirect:/login";
        }

        try {
            Payment payment = paymentService.getPaymentById(paymentId);

            // Check authorization
            if (role != null && role.equals("ADMIN")) {
                model.addAttribute("isAdmin", true);
            } else {
                User user = userService.findByEmail(email);
                if (!payment.getOrder().getUser().getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                    return "redirect:/payments/my";
                }
                model.addAttribute("isAdmin", false);
            }

            model.addAttribute("payment", payment);
            return "payment-view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return role != null && role.equals("ADMIN") ?
                    "redirect:/payments/admin/all" : "redirect:/payments/my";
        }
    }

    // List user's payments
    @GetMapping("/my")
    public String listMyPayments(HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view payments");
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            List<Payment> payments = paymentService.getUserPayments(user.getId());
            model.addAttribute("payments", payments);
            return "payment-list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    // Admin: List all payments
    @GetMapping("/admin/all")
    public String listAllPayments(HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        List<Payment> payments = paymentService.getAllPayments();
        model.addAttribute("payments", payments);
        return "payment-admin-list";
    }

    // Admin: Approve payment
    @PostMapping("/admin/{paymentId}/approve")
    public String approvePayment(@PathVariable Long paymentId,
                                 @RequestParam(required = false) String notes,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        try {
            paymentService.approvePayment(paymentId, email, notes);
            redirectAttributes.addFlashAttribute("message", "Payment approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payments/admin/all";
    }

    // Admin: Reject payment
    @PostMapping("/admin/{paymentId}/reject")
    public String rejectPayment(@PathVariable Long paymentId,
                                @RequestParam String notes,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        try {
            paymentService.rejectPayment(paymentId, email, notes);
            redirectAttributes.addFlashAttribute("message", "Payment rejected!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payments/admin/all";
    }

    // Admin: Refund payment
    @PostMapping("/admin/{paymentId}/refund")
    public String refundPayment(@PathVariable Long paymentId,
                                @RequestParam String notes,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        String email = (String) session.getAttribute("userEmail");

        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        try {
            paymentService.refundPayment(paymentId, email, notes);
            redirectAttributes.addFlashAttribute("message", "Payment refunded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payments/admin/all";
    }
}