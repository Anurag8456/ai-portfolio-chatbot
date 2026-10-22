package com.portfoliochatbot.backend;

import com.portfoliochatbot.backend.service.ChatbotService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class ChatbotServiceTest {

    @Test
    void testMissingApiKeyReturnsHelpfulMessage() {
        RestTemplate restTemplate = new RestTemplate();
        ChatbotService service = new ChatbotService("", "gemini-1.5-flash", restTemplate);

        String response = service.generateResponse("Hello");

        assertTrue(response.contains("Gemini API key is not configured"));
    }

    @Test
    void testPlaceholderApiKeyReturnsHelpfulMessage() {
        RestTemplate restTemplate = new RestTemplate();
        ChatbotService service = new ChatbotService("YOUR_GEMINI_API_KEY_HERE", "gemini-1.5-flash", restTemplate);

        String response = service.generateResponse("Hello");

        assertTrue(response.contains("Gemini API key is not configured"));
    }

    @Test
    void testEmptyPromptReturnsValidationMessage() {
        RestTemplate restTemplate = new RestTemplate();
        ChatbotService service = new ChatbotService("valid-key", "gemini-1.5-flash", restTemplate);

        String response = service.generateResponse("   ");

        assertEquals("Prompt cannot be empty.", response);
    }
}
