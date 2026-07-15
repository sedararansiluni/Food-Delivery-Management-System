package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.model.FeedBack;
import com.example.foodorderingsystem.service.FeedBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/feedbacks")
public class FeedBackController {

    @Autowired
    private FeedBackService feedBackService;

    @GetMapping
    public String listFeedbacks(Model model) {
        model.addAttribute("feedbacks", feedBackService.getAllFeedbacks());
        return "feedback-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("feedback", new FeedBack());
        return "feedback-create";
    }

    @PostMapping
    public String createFeedback(@ModelAttribute FeedBack feedBack,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            feedBackService.createFeedback(feedBack, file);
            redirectAttributes.addFlashAttribute("success", "Feedback created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating feedback: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            FeedBack feedBack = feedBackService.getFeedbackById(id)
                    .orElseThrow(() -> new RuntimeException("Feedback not found"));
            model.addAttribute("feedback", feedBack);
            return "feedback-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Feedback not found!");
            return "redirect:/feedbacks";
        }
    }

    @PostMapping("/{id}")
    public String updateFeedback(@PathVariable Long id,
                                 @ModelAttribute FeedBack feedBack,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            feedBackService.updateFeedback(id, feedBack, file);
            redirectAttributes.addFlashAttribute("success", "Feedback updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating feedback: " + e.getMessage());
        }
        return "redirect:/feedbacks";
    }

    @GetMapping("/{id}/delete")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedBackService.deleteFeedback(id);
            redirectAttributes.addFlashAttribute("success", "Feedback deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting feedback: " + e.getMessage());
        }
        return "redirect:/feedbacks";
    }


    @GetMapping("/documents/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveDocument(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads").resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error serving file: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/solve")
    public String markAsSolved(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            FeedBack feedBackToSolve = feedBackService.getFeedbackById(id)
                    .orElseThrow(() -> new RuntimeException("Feedback not found"));

            // Check if it's IN_PROGRESS - always allow
            if (feedBackToSolve.getStatus() == FeedBack.Status.IN_PROGRESS) {
                feedBackService.markAsSolved(id);
                redirectAttributes.addFlashAttribute("success", "Feedback marked as solved!");
                return "redirect:/feedbacks";
            }

            // For PENDING Feedback, check if it's the first one
            List<FeedBack> allFeedBacks = feedBackService.getAllFeedbacks();
            FeedBack firstPendingFeedBack = allFeedBacks.stream()
                    .filter(t -> t.getStatus() == FeedBack.Status.PENDING)
                    .findFirst()
                    .orElse(null);

            if (firstPendingFeedBack != null && firstPendingFeedBack.getId().equals(id)) {
                feedBackService.markAsSolved(id);
                redirectAttributes.addFlashAttribute("success", "Feedback marked as solved!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Please solve the first pending Feedback first!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating feedback: " + e.getMessage());
        }
        return "redirect:/feedbacks";
    }

    // Add this to your FeedbackController.java

    @GetMapping("/feedback-dashboard")
    public String showDashboard(Model model) {
        // Get all Feedbacks
        List<FeedBack> allFeedBacks = feedBackService.getAllFeedbacks();

        // Total Feedbacks
        model.addAttribute("totalFeedbacks", allFeedBacks.size());

        // Count by status
        long pendingCount = allFeedBacks.stream()
                .filter(t -> t.getStatus() == FeedBack.Status.PENDING)
                .count();
        long inProgressCount = allFeedBacks.stream()
                .filter(t -> t.getStatus() == FeedBack.Status.IN_PROGRESS)
                .count();
        long solvedCount = allFeedBacks.stream()
                .filter(t -> t.getStatus() == FeedBack.Status.SOLVED)
                .count();
        long closedCount = allFeedBacks.stream()
                .filter(t -> t.getStatus() == FeedBack.Status.CLOSED)
                .count();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("solvedCount", solvedCount);
        model.addAttribute("closedCount", closedCount);

        // Count by category
        // Count by category using the updated enum
        long foodQualityCount = allFeedBacks.stream()
                .filter(t -> t.getCategory() == FeedBack.Category.FOOD_QUALITY)
                .count();
        long deliveryIssueCount = allFeedBacks.stream()
                .filter(t -> t.getCategory() == FeedBack.Category.DELIVERY_ISSUE)
                .count();
        long orderAccuracyCount = allFeedBacks.stream()
                .filter(t -> t.getCategory() == FeedBack.Category.ORDER_ACCURACY)
                .count();
        long paymentIssueCount = allFeedBacks.stream()
                .filter(t -> t.getCategory() == FeedBack.Category.PAYMENT_ISSUE)
                .count();
        long appFeedbackCount = allFeedBacks.stream()
                .filter(t -> t.getCategory() == FeedBack.Category.APP_FEEDBACK)
                .count();
        long generalInquiryCount = allFeedBacks.stream()
                .filter(t -> t.getCategory() == FeedBack.Category.GENERAL_INQUIRY)
                .count();

        model.addAttribute("foodQualityCount", foodQualityCount);
        model.addAttribute("deliveryIssueCount", deliveryIssueCount);
        model.addAttribute("orderAccuracyCount", orderAccuracyCount);
        model.addAttribute("paymentIssueCount", paymentIssueCount);
        model.addAttribute("appFeedbackCount", appFeedbackCount);
        model.addAttribute("generalInquiryCount", generalInquiryCount);



        // Get recent Feedbacks (last 5)
        List<FeedBack> recentFeedBacks = allFeedBacks.stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("recentFeedbacks", recentFeedBacks);

        return "feedback-dashboard";
    }
}
