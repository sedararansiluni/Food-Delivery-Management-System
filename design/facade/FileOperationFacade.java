package com.example.foodorderingsystem.design.facade;

import com.example.foodorderingsystem.design.strategy.FileStorageStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileOperationFacade {
    private final FileStorageStrategy storageStrategy;

    public FileOperationFacade(@Qualifier("localFileStorageStrategy") FileStorageStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    public String handleFileSave(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return storageStrategy.saveFile(file);
    }

    public void handleFileDelete(String fileName) throws IOException {
        if (fileName != null && storageStrategy.fileExists(fileName)) {
            storageStrategy.deleteFile(fileName);
        }
    }

    public void handleFileUpdate(String oldFileName, MultipartFile newFile) throws IOException {
        handleFileDelete(oldFileName);
        handleFileSave(newFile);
    }
}

