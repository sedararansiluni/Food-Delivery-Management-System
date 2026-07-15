package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.model.Cart;
import com.example.foodorderingsystem.model.Product;
import com.example.foodorderingsystem.model.User;
import com.example.foodorderingsystem.service.CartService;
import com.example.foodorderingsystem.service.ProductService;
import com.example.foodorderingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    // List all products (Admin view)
    @GetMapping
    public String listProducts(Model model, HttpSession session) {
        model.addAttribute("products", productService.getAllProducts());

        // Add cart item count for logged-in users
        String email = (String) session.getAttribute("userEmail");
        if (email != null) {
            try {
                User user = userService.findByEmail(email);
                Cart cart = cartService.getOrCreateCart(user.getId());
                model.addAttribute("cartItemCount", cart.getTotalItems());
            } catch (Exception e) {
                model.addAttribute("cartItemCount", 0);
            }
        }

        return "product-list";
    }

    // Show add product form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-add";
    }

    // Save new product
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam(value = "image", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(product, imageFile);
            redirectAttributes.addFlashAttribute("message", "Product added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // Show edit product form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "product-edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Product not found!");
            return "redirect:/products";
        }
    }

    // Update product
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("product") Product product,
                                @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            product.setId(id);
            Optional<Product> existingProduct = productService.getProductById(id);
            if (existingProduct.isPresent() && (imageFile == null || imageFile.isEmpty())) {
                product.setImagePath(existingProduct.get().getImagePath());
                productService.saveProduct(product, null);
            } else {
                productService.updateProduct(product, imageFile);
            }
            redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // View product details
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());

            // Add cart item count
            String email = (String) session.getAttribute("userEmail");
            if (email != null) {
                try {
                    User user = userService.findByEmail(email);
                    Cart cart = cartService.getOrCreateCart(user.getId());
                    model.addAttribute("cartItemCount", cart.getTotalItems());
                } catch (Exception e) {
                    model.addAttribute("cartItemCount", 0);
                }
            }

            return "product-view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Product not found!");
            return "redirect:/products";
        }
    }

    // Delete product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product!");
        }
        return "redirect:/products";
    }

    // Update stock
    @PostMapping("/updateStock/{id}")
    public String updateStock(@PathVariable Long id,
                              @RequestParam Integer quantity,
                              RedirectAttributes redirectAttributes) {
        Product product = productService.updateStock(id, quantity);
        if (product != null) {
            redirectAttributes.addFlashAttribute("message", "Stock updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Product not found!");
        }
        return "redirect:/products";
    }

    // List all products for users (Customer view)
    @GetMapping("/user/list")
    public String listProductsUser(Model model, HttpSession session) {
        model.addAttribute("products", productService.getInStockProducts());

        // Add cart item count
        String email = (String) session.getAttribute("userEmail");
        if (email != null) {
            try {
                User user = userService.findByEmail(email);
                Cart cart = cartService.getOrCreateCart(user.getId());
                model.addAttribute("cartItemCount", cart.getTotalItems());
            } catch (Exception e) {
                model.addAttribute("cartItemCount", 0);
            }
        }

        return "product-user-list";
    }
}