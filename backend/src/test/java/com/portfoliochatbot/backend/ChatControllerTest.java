package com.portfoliochatbot.backend;

import com.portfoliochatbot.backend.controller.ChatController;
import com.portfoliochatbot.backend.dto.ChatResponse;
import com.portfoliochatbot.backend.dto.JobMatchResponse;
import com.portfoliochatbot.backend.service.ChatbotService;
import com.portfoliochatbot.backend.service.JobDescriptionParserService;
import com.portfoliochatbot.backend.service.ResumeParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ResumeParserService resumeParserService;

    @Mock
    private JobDescriptionParserService jobDescriptionParserService;

    @Mock
    private ChatbotService chatbotService;

    @InjectMocks
    private ChatController chatController;

    @Test
    void testHealthCheck() {
        ResponseEntity<String> response = chatController.healthCheck();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Chat service is running", response.getBody());
    }

    @Test
    void testAskQuestionWithoutResumeFails() {
        ResponseEntity<ChatResponse> response = chatController.askQuestion(null, "What are your skills?", null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getErrorMessage().contains("Either resume file or resume text must be provided"));
    }

    @Test
    void testAskQuestionWithEmptyQuestionFails() {
        ResponseEntity<ChatResponse> response = chatController.askQuestion(null, "   ", "My resume text");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getErrorMessage().contains("Question cannot be empty"));
    }

    @Test
    void testAskQuestionSuccess() {
        when(chatbotService.answerHrQuestion(anyString(), anyString())).thenReturn("I have 3 years of Java experience.");

        ResponseEntity<ChatResponse> response = chatController.askQuestion(null, "Tell me about your experience", "Java developer resume");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("I have 3 years of Java experience.", response.getBody().getAnswer());
    }

    @Test
    void testMatchJobWithoutJobDescriptionFails() {
        ResponseEntity<JobMatchResponse> response = chatController.matchJob(null, "My resume text", null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getErrorMessage().contains("Either job description file or job description text must be provided"));
    }

    @Test
    void testMatchJobSuccess() {
        when(chatbotService.matchResumeWithJob(anyString(), anyString())).thenReturn("90% match - Excellent candidate");

        ResponseEntity<JobMatchResponse> response = chatController.matchJob(null, "Resume text", null, "Job description text");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("90% match - Excellent candidate", response.getBody().getAnalysis());
    }
}
