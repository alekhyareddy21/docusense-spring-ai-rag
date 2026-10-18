package com.docusense.ai.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PdfProcessingService.class);

    private static final int CHUNK_SIZE = 600;
    private static final int CHUNK_OVERLAP = 100;

    public record DocumentChunk(int index, String text, int startChar, int endChar) {}

    public String extractText(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType != null && contentType.equalsIgnoreCase("application/pdf")
                || (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf"))) {
            return extractTextFromPdf(file.getBytes());
        } else {
            // Fallback for .txt or .md files
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(pdfBytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF ({} pages)", text.length(), document.getNumberOfPages());
            return text;
        }
    }

    public List<DocumentChunk> chunkText(String fullText) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (fullText == null || fullText.isBlank()) {
            return chunks;
        }

        String cleaned = fullText.replaceAll("\\r\\n", "\n").replaceAll("\\s+", " ").trim();
        int length = cleaned.length();
        int start = 0;
        int chunkIndex = 0;

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);

            // Avoid cutting words in half if possible
            if (end < length) {
                int lastSpace = cleaned.lastIndexOf(' ', end);
                if (lastSpace > start + (CHUNK_SIZE / 2)) {
                    end = lastSpace;
                }
            }

            String chunkContent = cleaned.substring(start, end).trim();
            if (!chunkContent.isBlank()) {
                chunks.add(new DocumentChunk(chunkIndex++, chunkContent, start, end));
            }

            if (end >= length) {
                break;
            }
            start = Math.max(start + 1, end - CHUNK_OVERLAP);
        }

        log.info("Split document into {} chunks (size={}, overlap={})", chunks.size(), CHUNK_SIZE, CHUNK_OVERLAP);
        return chunks;
    }
}
