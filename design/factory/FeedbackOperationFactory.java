package com.example.foodorderingsystem.design.factory;

import com.example.foodorderingsystem.design.template.MarkAsPendingOperation;
import com.example.foodorderingsystem.design.template.MarkAsSolvedOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeedbackOperationFactory {
    @Autowired
    private MarkAsSolvedOperation markAsSolvedOperation;

    @Autowired
    private MarkAsPendingOperation markAsPendingOperation;

    public FeedbackOperation getOperation(String operationType) {
        switch (operationType.toUpperCase()) {
            case "SOLVED":
                return markAsSolvedOperation;
            case "PENDING":
                return markAsPendingOperation;
            default:
                throw new IllegalArgumentException("Unknown operation type: " + operationType);
        }
    }
}