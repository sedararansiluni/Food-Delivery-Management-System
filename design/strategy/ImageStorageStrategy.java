package com.example.foodorderingsystem.design.strategy;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageStrategy {
    String saveImage(MultipartFile file) throws IOException;
    void deleteImage(String imagePath) throws IOException;
}