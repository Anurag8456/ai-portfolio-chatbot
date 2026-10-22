package com.portfoliochatbot.backend;

import com.portfoliochatbot.backend.service.ResumeParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ResumeParserServiceTest {

    private ResumeParserService resumeParserService;

    @BeforeEach
    void setUp() {
        resumeParserService = new ResumeParserService();
    }

    @Test
    void testUnsupportedFormatThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain", "Some resume content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resumeParserService.extractText(file)
        );

        assertTrue(exception.getMessage().contains("Unsupported file format"));
    }

    @Test
    void testNullFilenameThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", null, "application/pdf", new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resumeParserService.extractText(file)
        );

        assertTrue(exception.getMessage().contains("File must have a name"));
    }
}
