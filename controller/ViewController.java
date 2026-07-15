package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.dto.UserProfileResponse;
import com.example.foodorderingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.foodorderingsystem.model.User;
import java.util.List;

@Controller
public class ViewController {

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String homePage() {
        return "home"; // maps to /WEB-INF/jsp/home.jsp
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // maps to /WEB-INF/jsp/login.jsp
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // maps to /WEB-INF/jsp/register.jsp
    }

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session, Model model) {
        // Get user email from session (set during login)
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        // Load fresh profile from DB/service
        UserProfileResponse profile = userService.getUserProfile(email);

        // Add to model for JSP rendering
        model.addAttribute("userProfile", profile);

        return "dashboard"; // maps to /WEB-INF/jsp/dashboard.jsp
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage(HttpSession session, Model model) {
        // Get user email from session (set during login)
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        // Load fresh profile from DB/service
        UserProfileResponse profile = userService.getUserProfile(email);

        // Add to model for JSP rendering
        model.addAttribute("userProfile", profile);

        return "admin-dashboard"; // maps to /WEB-INF/jsp/dashboard.jsp
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        UserProfileResponse profile = userService.getUserProfile(email);
        model.addAttribute("userProfile", profile);

        return "profile"; // maps to /WEB-INF/jsp/profile.jsp
    }

    @GetMapping("/admin/users")
    public String listUsersAdmin(Model model) {
        List<User> users = userService.getAllUsers(); // Make sure you have a service method to fetch all users
        model.addAttribute("users", users);
        return "user-list-admin";
    }
}