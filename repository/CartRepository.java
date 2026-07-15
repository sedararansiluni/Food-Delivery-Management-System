package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.model.Cart;
import com.example.foodorderingsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findByUserId(Long userId);
    void deleteByUser(User user);
}