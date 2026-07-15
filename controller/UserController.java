package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.dto.RegisterRequest;
import com.example.foodorderingsystem.dto.LoginResponse;
import com.example.foodorderingsystem.dto.UserProfileResponse;
import com.example.foodorderingsystem.model.LoginRequest;
import com.example.foodorderingsystem.model.User;
import com.example.foodorderingsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.registerUser(registerRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest,
                                       HttpServletRequest request) {
        try {
            LoginResponse loginResponse = userService.loginUser(loginRequest);

            // Create session and store user info
            HttpSession session = request.getSession(true);
            session.setAttribute("userEmail", loginResponse.getEmail());
            session.setAttribute("userRole", loginResponse.getRole());
            session.setAttribute("token", loginResponse.getToken());
            session.setMaxInactiveInterval(24 * 60 * 60); // 24 hours

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/user/profile")
    public ResponseEntity<?> getUserProfile(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No active session"));
        }
        UserProfileResponse profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }


    @PutMapping("/user/profile")
    public ResponseEntity<?> updateUserProfile(HttpSession session,
                                               @RequestBody RegisterRequest updateRequest) {
        String oldEmail = (String) session.getAttribute("userEmail");
        if (oldEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No active session"));
        }

        UserProfileResponse updatedProfile = userService.updateUserProfile(oldEmail, updateRequest);

        // Update session email if it changed
        session.setAttribute("userEmail", updatedProfile.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "profile", updatedProfile
        ));
    }


    @PostMapping("/auth/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // Clear security context
            SecurityContextHolder.clearContext();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Logout failed");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity<?> deleteUser(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No active session"));
        }

        try {
            userService.deleteUser(email); // Call the service method to delete the user
            session.invalidate(); // Invalidate the session after deletion

            return ResponseEntity.ok(Map.of(
                    "message", "User account deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}