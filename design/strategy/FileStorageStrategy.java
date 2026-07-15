package com.example.foodorderingsystem.design.strategy;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageStrategy {
    String saveFile(MultipartFile file) throws IOException;
    void deleteFile(String fileName) throws IOException;
    boolean fileExists(String fileName);
}