package com.portfoliochatbot.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    private final RestTemplate restTemplate;
    private final String geminiApiKey;

    // Gemini API endpoint
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent";

    // Removed ObjectMapper from the constructor
    public ChatbotService(
            @Value("${gemini.api.key}") String geminiApiKey,
            RestTemplate restTemplate) {
        this.geminiApiKey = geminiApiKey;
        this.restTemplate = restTemplate;
    }

    /**
     * Generate a response from Gemini API based on the prompt
     * @param prompt The user's question/prompt
     * @return Gemini's response text
     */
    public String generateResponse(String prompt) {
        // Create the request body for Gemini API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents",
                new Object[]{
                        Map.of("parts",
                                new Object[]{
                                        Map.of("text", prompt)
                                })
                }
        );

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Make the API call
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + geminiApiKey,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // Extract the text response from Gemini's response
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody != null && responseBody.containsKey("candidates")) {
            @SuppressWarnings("unchecked")
            var candidates = (java.util.List<Map<String, Object>>) responseBody.get("candidates");
            if (!candidates.isEmpty()) {
                Map<String, Object> firstCandidate = candidates.get(0);
                if (firstCandidate.containsKey("content")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                    if (content.containsKey("parts")) {
                        @SuppressWarnings("unchecked")
                        var parts = (java.util.List<Map<String, Object>>) content.get("parts");
                        if (!parts.isEmpty()) {
                            Map<String, Object> firstPart = parts.get(0);
                            if (firstPart.containsKey("text")) {
                                return (String) firstPart.get("text");
                            }
                        }
                    }
                }
            }
        }

        // Fallback if we couldn't parse the response properly
        return "I'm sorry, I couldn't generate a response at the moment. Please try again.";
    }

    /**
     * Answer HR questions about you using your resume data
     * @param question The HR's question
     * @param resumeText The extracted text from your resume
     * @return Answer to the question based on your resume
     */
    public String answerHrQuestion(String question, String resumeText) {
        // Create a comprehensive prompt that instructs Gemini to answer based ONLY on the resume
        String prompt = """
                  You are an AI assistant representing Anurag Singh. Your task is to answer HR questions about Anurag's background, skills, experience, and qualifications.

                  IMPORTANT RULES:
                  1. Answer ONLY based on the information provided in the resume text below
                  2. If the information is not in the resume, say "I don't have that information in my resume"
                  3. Be honest, accurate, and professional
                  4. Do not make up or infer information that isn't explicitly stated
                  5. Keep answers concise but informative

                  RESUME TEXT:
                  """ + resumeText + """
                  
                  HR QUESTION: """ + question + """
                  
                  ANSWER:
                  """;

        return generateResponse(prompt);
    }
}