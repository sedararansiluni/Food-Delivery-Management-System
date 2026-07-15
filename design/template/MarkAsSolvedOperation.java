package com.example.foodorderingsystem.design.template;

import com.example.foodorderingsystem.design.factory.FeedbackOperation;
import com.example.foodorderingsystem.model.FeedBack;
import com.example.foodorderingsystem.repository.FeedbackRepository;
import org.springframework.stereotype.Component;

@Component
public class MarkAsSolvedOperation extends FeedbackOperation {

    public MarkAsSolvedOperation(FeedbackRepository feedbackRepository) {
        super(feedbackRepository);
    }

    @Override
    protected void validateOperation(FeedBack feedBack) {
        if (feedBack.getStatus() == FeedBack.Status.SOLVED) {
            throw new RuntimeException("Feedback is already marked as solved");
        }
    }

    @Override
    protected void performOperation(FeedBack feedBack) {
        feedBack.setStatus(FeedBack.Status.SOLVED);
    }
}