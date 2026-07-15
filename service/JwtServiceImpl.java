package com.example.foodorderingsystem.service;


import com.example.foodorderingsystem.model.User;
import com.example.foodorderingsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getEmail());
    }

    @Override
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public boolean validateToken(String token, String email) {
        return jwtUtil.validateToken(token, email);
    }
}