package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.model.*;
import com.example.foodorderingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Order createOrder(Long userId, String deliveryAddress, String phoneNumber, String specialInstructions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock availability
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (!product.getInStock() || product.getAvailability() < cartItem.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock or insufficient quantity");
            }
        }

        // Create order
        String orderNumber = generateOrderNumber();
        Double totalAmount = Double.valueOf(cart.getTotalAmount());

        Order order = new Order(user, orderNumber, totalAmount, deliveryAddress, phoneNumber);
        order.setSpecialInstructions(specialInstructions);
        order = orderRepository.save(order);

        // Create order items and update product availability
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getPrice()
            );
            order.addItem(orderItem);
            orderItemRepository.save(orderItem);

            // Update product availability
            product.setAvailability(product.getAvailability() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart after order creation
        cart.clearItems();
        cartRepository.save(cart);

        return order;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Force initialization of lazy-loaded collections
        order.getOrderItems().size(); // This triggers the loading of order items
        order.getOrderItems().forEach(item -> {
            // Force loading of product for each order item
            item.getProduct().getName();
        });

        return order;
    }

    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();

        // Force initialization of lazy-loaded collections for all orders
        orders.forEach(order -> {
            order.getOrderItems().size();
            order.getOrderItems().forEach(item -> item.getProduct().getName());
        });

        return orders;
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public void cancelOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId);

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel this order");
        }

        // Restore product availability
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setAvailability(product.getAvailability() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD-" + timestamp + "-" + (int)(Math.random() * 1000);
    }
}