package com.portfoliochatbot.backend;

import com.portfoliochatbot.backend.service.JobDescriptionParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class JobDescriptionParserServiceTest {

    private JobDescriptionParserService jobDescriptionParserService;

    @BeforeEach
    void setUp() {
        jobDescriptionParserService = new JobDescriptionParserService();
    }

    @Test
    void testUnsupportedFormatThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "job_description.txt", "text/plain", "Software Engineer Job".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jobDescriptionParserService.extractText(file)
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
                () -> jobDescriptionParserService.extractText(file)
        );

        assertTrue(exception.getMessage().contains("File must have a name"));
    }
}
