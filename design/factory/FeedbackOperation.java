package com.example.foodorderingsystem.design.factory;

import com.example.foodorderingsystem.model.FeedBack;
import com.example.foodorderingsystem.repository.FeedbackRepository;

public abstract class FeedbackOperation {
    protected final FeedbackRepository feedbackRepository;

    public FeedbackOperation(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    // Template method
    public final FeedBack executeOperation(Long feedbackId) {
        FeedBack feedBack = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + feedbackId));

        validateOperation(feedBack);
        performOperation(feedBack);
        return feedbackRepository.save(feedBack);
    }

    // Hook - subclasses can override if needed validation
    protected void validateOperation(FeedBack feedBack) {
        // Default: no validation. Subclasses can override
    }

    // Abstract method - must be implemented by subclasses
    protected abstract void performOperation(FeedBack feedBack);
}