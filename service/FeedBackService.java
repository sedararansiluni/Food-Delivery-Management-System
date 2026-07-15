package com.example.foodorderingsystem.service;

import com.example.foodorderingsystem.design.facade.FileOperationFacade;
import com.example.foodorderingsystem.design.factory.FeedbackOperation;
import com.example.foodorderingsystem.design.factory.FeedbackOperationFactory;
import com.example.foodorderingsystem.design.template.MarkAsPendingOperation;
import com.example.foodorderingsystem.model.FeedBack;
import com.example.foodorderingsystem.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FeedBackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FileOperationFacade fileOperationFacade;

    @Autowired
    private FeedbackOperationFactory feedbackOperationFactory;

    @Autowired
    private MarkAsPendingOperation markAsPendingOperation;

    public List<FeedBack> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderByCreatedAtAsc();
    }

    public Optional<FeedBack> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public FeedBack createFeedback(FeedBack feedBack, MultipartFile file) throws IOException {
        String fileName = fileOperationFacade.handleFileSave(file);
        if (fileName != null) {
            feedBack.setDocumentPath(fileName);
        }
        return feedbackRepository.save(feedBack);
    }

    public FeedBack updateFeedback(Long id, FeedBack feedBackDetails, MultipartFile file) throws IOException {
        FeedBack feedBack = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));

        // Update all fields
        feedBack.setCustomerName(feedBackDetails.getCustomerName());
        feedBack.setContactNumber(feedBackDetails.getContactNumber());
        feedBack.setEmail(feedBackDetails.getEmail());
        feedBack.setSubject(feedBackDetails.getSubject());
        feedBack.setConcern(feedBackDetails.getConcern());
        feedBack.setCategory(feedBackDetails.getCategory());
        feedBack.setStatus(feedBackDetails.getStatus());

        // Handle file update
        if (file != null && !file.isEmpty()) {
            fileOperationFacade.handleFileDelete(feedBack.getDocumentPath());
            String fileName = fileOperationFacade.handleFileSave(file);
            if (fileName != null) {
                feedBack.setDocumentPath(fileName);
            }
        }

        return feedbackRepository.save(feedBack);
    }

    public void deleteFeedback(Long id) {
        FeedBack feedBack = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));

        try {
            fileOperationFacade.handleFileDelete(feedBack.getDocumentPath());
        } catch (IOException e) {
            throw new RuntimeException("Error deleting feedback file", e);
        }

        feedbackRepository.delete(feedBack);
    }

    public FeedBack updateFeedbackStatus(Long id, String operationType) {
        FeedBack feedBack = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));

        FeedbackOperation operation = feedbackOperationFactory.getOperation(operationType);

        return feedbackRepository.save(feedBack);
    }

    public FeedBack markAsSolved(Long id) {
        FeedBack feedBack = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
        feedBack.setStatus(FeedBack.Status.SOLVED);
        return feedbackRepository.save(feedBack);
    }

}