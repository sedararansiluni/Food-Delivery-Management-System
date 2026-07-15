package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.model.User;

public interface JwtService {
    String generateToken(User user);
    String extractUsername(String token);
    boolean validateToken(String token, String email);
}