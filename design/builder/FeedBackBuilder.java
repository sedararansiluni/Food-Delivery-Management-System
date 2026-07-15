package com.example.foodorderingsystem.design.builder;

import com.example.foodorderingsystem.model.FeedBack;

public class FeedBackBuilder {
    private final FeedBack feedBack;

    public FeedBackBuilder() {
        this.feedBack = new FeedBack();
    }

    public FeedBackBuilder withCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        feedBack.setCustomerName(customerName);
        return this;
    }

    public FeedBackBuilder withContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact number cannot be null or empty");
        }
        feedBack.setContactNumber(contactNumber);
        return this;
    }

    public FeedBackBuilder withEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        feedBack.setEmail(email);
        return this;
    }

    public FeedBackBuilder withSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        feedBack.setSubject(subject);
        return this;
    }

    public FeedBackBuilder withConcern(String concern) {
        if (concern == null || concern.trim().isEmpty()) {
            throw new IllegalArgumentException("Concern cannot be null or empty");
        }
        feedBack.setConcern(concern);
        return this;
    }

    public FeedBackBuilder withCategory(FeedBack.Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        feedBack.setCategory(category);
        return this;
    }

    public FeedBackBuilder withStatus(FeedBack.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        feedBack.setStatus(status);
        return this;
    }

    public FeedBackBuilder withDocumentPath(String documentPath) {
        feedBack.setDocumentPath(documentPath);
        return this;
    }

    public FeedBack build() {
        // Validation before building
        if (feedBack.getCustomerName() == null) {
            throw new IllegalStateException("Customer name is required");
        }
        if (feedBack.getEmail() == null) {
            throw new IllegalStateException("Email is required");
        }
        if (feedBack.getCategory() == null) {
            throw new IllegalStateException("Category is required");
        }
        if (feedBack.getStatus() == null) {
            feedBack.setStatus(FeedBack.Status.PENDING); // Default status
        }
        return feedBack;
    }
}

