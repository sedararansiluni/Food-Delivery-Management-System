package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.model.Cart;
import com.example.foodorderingsystem.model.User;
import com.example.foodorderingsystem.service.CartService;
import com.example.foodorderingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewCart(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to view cart");
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            Cart cart = cartService.getOrCreateCart(user.getId());
            model.addAttribute("cart", cart);
            return "cart-view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to add items to cart");
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            cartService.addToCart(user.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("message", "Product added to cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/products/user/list";
    }

    @PostMapping("/update/{itemId}")
    public String updateCartItem(@PathVariable Long itemId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            cartService.updateCartItemQuantity(user.getId(), itemId, quantity);
            redirectAttributes.addFlashAttribute("message", "Cart updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    @GetMapping("/remove/{itemId}")
    public String removeFromCart(@PathVariable Long itemId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            cartService.removeFromCart(user.getId(), itemId);
            redirectAttributes.addFlashAttribute("message", "Item removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByEmail(email);
            cartService.clearCart(user.getId());
            redirectAttributes.addFlashAttribute("message", "Cart cleared successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }
}