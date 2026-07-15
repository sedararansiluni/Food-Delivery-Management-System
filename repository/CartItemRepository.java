package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.model.Cart;
import com.example.foodorderingsystem.model.CartItem;
import com.example.foodorderingsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}