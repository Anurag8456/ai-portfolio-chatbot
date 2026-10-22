package com.portfoliochatbot.backend.controller;

import com.portfoliochatbot.backend.dto.ChatResponse;
import com.portfoliochatbot.backend.dto.JobMatchResponse;
import com.portfoliochatbot.backend.service.ChatbotService;
import com.portfoliochatbot.backend.service.JobDescriptionParserService;
import com.portfoliochatbot.backend.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174", "http://127.0.0.1:5173"})
public class ChatController {

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private JobDescriptionParserService jobDescriptionParserService;

    @Autowired
    private ChatbotService chatbotService;

    /**
     * Endpoint for chatting with the AI about your resume
     * Accepts either:
     * 1. A resume file (PDF/DOCX) + question text
     * 2. Question text + pre-extracted resume text (for testing)
     */
    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(
            @RequestPart(required = false) MultipartFile resumeFile,
            @RequestParam String question,
            @RequestParam(required = false) String resumeText) {

        ChatResponse response = new ChatResponse();
        String extractedResumeText;

        try {
            // Get resume text either from file upload or direct parameter
            if (resumeFile != null && !resumeFile.isEmpty()) {
                extractedResumeText = resumeParserService.extractText(resumeFile);
            } else if (resumeText != null && !resumeText.isEmpty()) {
                extractedResumeText = resumeText;
            } else {
                response.setSuccess(false);
                response.setErrorMessage("Either resume file or resume text must be provided");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Validate inputs
            if (question == null || question.trim().isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("Question cannot be empty");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Generate answer using chatbot service
            String answer = chatbotService.answerHrQuestion(question, extractedResumeText);

            response.setAnswer(answer);
            response.setSuccess(true);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorMessage("Error processing request: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint for matching candidate resume against a job description
     * Accepts:
     * 1. Resume file or resume text
     * 2. Job description file or job description text
     */
    @PostMapping("/match-job")
    public ResponseEntity<JobMatchResponse> matchJob(
            @RequestPart(required = false) MultipartFile resumeFile,
            @RequestParam(required = false) String resumeText,
            @RequestPart(required = false) MultipartFile jobDescriptionFile,
            @RequestParam(required = false) String jobDescriptionText) {

        JobMatchResponse response = new JobMatchResponse();
        String candidateResume;
        String jobDescription;

        try {
            // Extract or obtain resume text
            if (resumeFile != null && !resumeFile.isEmpty()) {
                candidateResume = resumeParserService.extractText(resumeFile);
            } else if (resumeText != null && !resumeText.isEmpty()) {
                candidateResume = resumeText;
            } else {
                response.setSuccess(false);
                response.setErrorMessage("Either resume file or resume text must be provided");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Extract or obtain job description text
            if (jobDescriptionFile != null && !jobDescriptionFile.isEmpty()) {
                jobDescription = jobDescriptionParserService.extractText(jobDescriptionFile);
            } else if (jobDescriptionText != null && !jobDescriptionText.isEmpty()) {
                jobDescription = jobDescriptionText;
            } else {
                response.setSuccess(false);
                response.setErrorMessage("Either job description file or job description text must be provided");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Perform matching analysis
            String analysis = chatbotService.matchResumeWithJob(candidateResume, jobDescription);
            response.setAnalysis(analysis);
            response.setSuccess(true);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorMessage("Error matching resume with job description: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Simple health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Chat service is running", HttpStatus.OK);
    }
}
