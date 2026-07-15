package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.model.Order;
import com.example.foodorderingsystem.model.OrderStatus;
import com.example.foodorderingsystem.model.User;
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
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listOrders(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view orders");
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            List<Order> orders = orderService.getUserOrders(user.getId());
            model.addAttribute("orders", orders);
            return "orders-list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/checkout")
    public String showCheckoutForm(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to checkout");
            return "redirect:/login";
        }

        return "orders-checkout";
    }

    @PostMapping("/create")
    public String createOrder(@RequestParam String deliveryAddress,
                              @RequestParam String phoneNumber,
                              @RequestParam(required = false) String specialInstructions,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            Order order = orderService.createOrder(user.getId(), deliveryAddress, phoneNumber, specialInstructions);
            redirectAttributes.addFlashAttribute("message", "Order placed successfully! Order Number: " + order.getOrderNumber());
            return "redirect:/payments/order/" + order.getId(); // Redirect to payment page
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/{orderId}")
    public String viewOrder(@PathVariable Long orderId,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            Order order = orderService.getOrderById(orderId);

            if (!order.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/orders";
            }

            model.addAttribute("order", order);
            return "orders-view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders";
        }
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            orderService.cancelOrder(orderId, user.getId());
            redirectAttributes.addFlashAttribute("message", "Order cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders/" + orderId;
    }

    // Admin functions
    @GetMapping("/admin/all")
    public String listAllOrders(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders-admin-list";
    }

    @PostMapping("/admin/{orderId}/updateStatus")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("userRole");
        if (role == null || !role.equals("ADMIN")) {
            redirectAttributes.addFlashAttribute("error", "Admin access required");
            return "redirect:/";
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            orderService.updateOrderStatus(orderId, orderStatus);
            redirectAttributes.addFlashAttribute("message", "Order status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders/admin/all";
    }

    @GetMapping("/admin/{orderId}")
    public String viewOrderAdmin(@PathVariable Long orderId,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        if (email == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.getOrderById(orderId);

            // Allow admin to view any order, or user to view their own order
            if (role != null && role.equals("ADMIN")) {
                model.addAttribute("order", order);
                model.addAttribute("isAdmin", true);
                return "orders-admin-view";
            } else {
                User user = userService.findByEmail(email);
                if (!order.getUser().getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                    return "redirect:/orders";
                }
                model.addAttribute("order", order);
                model.addAttribute("isAdmin", false);
                return "orders-view";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return role != null && role.equals("ADMIN") ? "redirect:/orders/admin/all" : "redirect:/orders";
        }
    }


}