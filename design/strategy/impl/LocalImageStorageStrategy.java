package com.example.foodorderingsystem.design.strategy.impl;

import com.example.foodorderingsystem.design.strategy.ImageStorageStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalImageStorageStrategy implements ImageStorageStrategy {
    private final String uploadDir = "uploads/products/";

    @Override
    public String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + newFilename;
    }

    @Override
    public void deleteImage(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        Files.deleteIfExists(path);
    }
}

