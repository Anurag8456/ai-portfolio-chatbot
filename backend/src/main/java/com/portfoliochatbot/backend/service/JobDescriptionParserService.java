package com.portfoliochatbot.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class JobDescriptionParserService {

    /**
     * Extract text from uploaded job description file (PDF or DOCX)
     * @param file The uploaded job description file
     * @return Extracted text content
     * @throws IOException If file processing fails
     */
    public String extractText(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a name");
        }

        String lowerCaseName = originalFilename.toLowerCase();

        try (InputStream inputStream = file.getInputStream()) {
            if (lowerCaseName.endsWith(".pdf")) {
                return extractTextFromPdf(inputStream);
            } else if (lowerCaseName.endsWith(".docx")) {
                return extractTextFromDocx(inputStream);
            } else {
                throw new IllegalArgumentException("Unsupported file format. Only PDF and DOCX are supported.");
            }
        }
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }
}
