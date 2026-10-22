package com.portfoliochatbot.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    private final RestTemplate restTemplate;
    private final String geminiApiKey;
    private final String geminiModel;

    public ChatbotService(
            @Value("${gemini.api.key:}") String geminiApiKey,
            @Value("${gemini.api.model:gemini-1.5-flash}") String geminiModel,
            RestTemplate restTemplate) {
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = (geminiModel == null || geminiModel.trim().isEmpty()) ? "gemini-1.5-flash" : geminiModel;
        this.restTemplate = restTemplate;
    }

    private String getGeminiApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent";
    }

    /**
     * Generate a response from Gemini API based on the prompt
     * @param prompt The user's question/prompt
     * @return Gemini's response text
     */
    public String generateResponse(String prompt) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || "your_gemini_api_key_here".equalsIgnoreCase(geminiApiKey)) {
            return "Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable or define it in application.properties.";
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            return "Prompt cannot be empty.";
        }

        try {
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
                    getGeminiApiUrl() + "?key=" + geminiApiKey,
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

            return "I'm sorry, I couldn't generate a response at the moment. Please try again.";

        } catch (HttpStatusCodeException e) {
            return "Gemini API error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Failed to communicate with AI service: " + e.getMessage();
        }
    }

    /**
     * Answer HR questions about you using your resume data
     * @param question The HR's question
     * @param resumeText The extracted text from your resume
     * @return Answer to the question based on your resume
     */
    public String answerHrQuestion(String question, String resumeText) {
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

    /**
     * Analyze and match candidate resume against a job description
     * @param resumeText The candidate's resume text
     * @param jobDescriptionText The job description text
     * @return Detailed match analysis
     */
    public String matchResumeWithJob(String resumeText, String jobDescriptionText) {
        String prompt = """
                  You are an expert technical recruiter and career advisor evaluating candidate Anurag Singh for a specific job role.

                  Compare the candidate's resume against the provided Job Description (JD) and provide a thorough, structured match analysis.

                  Please structure your response with the following sections:
                  1. \uD83C\uDFAF Overall Match Score: (e.g. 85%) and 2-sentence executive summary
                  2. ✅ Matching Strengths & Skills: Direct alignments with requirements
                  3. ⚠️ Skill Gaps & Areas to Improve: Missing keywords or experience gaps
                  4. \uD83D\uDCA1 Interview Preparation Advice: Questions the recruiter is likely to ask based on this JD
                  5. \uD83D\uDCCB Hiring Recommendation: Final verdict (Strong Match, Moderate Match, or Stretch)

                  CANDIDATE RESUME:
                  """ + resumeText + """
                  
                  JOB DESCRIPTION:
                  """ + jobDescriptionText + """
                  """;

        return generateResponse(prompt);
    }
}
