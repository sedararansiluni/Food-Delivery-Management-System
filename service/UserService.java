package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.dto.RegisterRequest;
import com.example.foodorderingsystem.dto.LoginResponse;
import com.example.foodorderingsystem.dto.UserProfileResponse;
import com.example.foodorderingsystem.model.LoginRequest;
import com.example.foodorderingsystem.model.User;

import java.util.List;

public interface UserService {
    User registerUser(RegisterRequest registerRequest);
    LoginResponse loginUser(LoginRequest loginRequest);
    UserProfileResponse getUserProfile(String email);
    UserProfileResponse updateUserProfile(String email, RegisterRequest updateRequest);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteUser(String email);
    List<User> getAllUsers();
}