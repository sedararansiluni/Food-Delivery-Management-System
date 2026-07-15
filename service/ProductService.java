package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.design.strategy.ImageStorageStrategy;
import com.example.foodorderingsystem.model.Product;
import com.example.foodorderingsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    @Qualifier("localImageStorageStrategy")
    private ImageStorageStrategy imageStorageStrategy;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = imageStorageStrategy.saveImage(imageFile);
            product.setImagePath(imagePath);
        }
        product.setInStock(product.getAvailability() > 0);
        return productRepository.save(product);
    }

    public Product updateProduct(Product product, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                imageStorageStrategy.deleteImage(product.getImagePath());
            }
            String imagePath = imageStorageStrategy.saveImage(imageFile);
            product.setImagePath(imagePath);
        }
        product.setInStock(product.getAvailability() > 0);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && product.get().getImagePath() != null) {
            try {
                imageStorageStrategy.deleteImage(product.get().getImagePath());
            } catch (IOException e) {
                throw new RuntimeException("Error deleting product image", e);
            }
        }
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getInStockProducts() {
        return productRepository.findByInStock(true);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Product updateStock(Long id, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setAvailability(quantity);
            product.setInStock(quantity > 0);
            return productRepository.save(product);
        }
        return null;
    }
}