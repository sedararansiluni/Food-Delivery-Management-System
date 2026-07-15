package com.example.foodorderingsystem.design.template;

import com.example.foodorderingsystem.design.factory.FeedbackOperation;
import com.example.foodorderingsystem.model.FeedBack;
import com.example.foodorderingsystem.repository.FeedbackRepository;
import org.springframework.stereotype.Component;

@Component
public class MarkAsPendingOperation extends FeedbackOperation {

    public MarkAsPendingOperation(FeedbackRepository feedbackRepository) {
        super(feedbackRepository);
    }

    @Override
    public void validateOperation(FeedBack feedBack) {
        if (feedBack.getStatus() == FeedBack.Status.PENDING) {
            throw new RuntimeException("Feedback is already pending");
        }
    }

    @Override
    protected void performOperation(FeedBack feedBack) {
        feedBack.setStatus(FeedBack.Status.PENDING);
    }
}

