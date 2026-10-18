package com.docusense.ai.service;

import com.docusense.ai.dto.DocumentDto;
import com.docusense.ai.entity.DocumentEntity;
import com.docusense.ai.entity.DocumentStatus;
import com.docusense.ai.entity.UserEntity;
import com.docusense.ai.repository.DocumentRepository;
import com.docusense.ai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final PdfProcessingService pdfProcessingService;
    private final VectorStoreService vectorStoreService;

    public DocumentService(DocumentRepository documentRepository,
                           UserRepository userRepository,
                           PdfProcessingService pdfProcessingService,
                           VectorStoreService vectorStoreService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.pdfProcessingService = pdfProcessingService;
        this.vectorStoreService = vectorStoreService;
    }

    @Transactional
    public DocumentDto uploadAndIndex(MultipartFile file, Long userId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "uploaded_file_" + System.currentTimeMillis();
        }

        UserEntity user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null) {
            // Default demo user if none exists
            user = userRepository.findByEmail("alekhya@example.com").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email("alekhya@example.com")
                        .fullName("Budati Alekhya")
                        .role("ROLE_DEVELOPER")
                        .build())
            );
        }

        // 1. Save Document Entity via Hibernate JPA
        DocumentEntity entity = DocumentEntity.builder()
                .fileName(originalFilename)
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .fileSizeBytes(file.getSize())
                .status(DocumentStatus.PROCESSING)
                .chunkCount(0)
                .user(user)
                .build();
        entity = documentRepository.save(entity);

        try {
            // 2. Parse text from PDF/Text
            String text = pdfProcessingService.extractText(file);

            // 3. Chunk text
            List<PdfProcessingService.DocumentChunk> chunks = pdfProcessingService.chunkText(text);

            // 4. Index in Vector Store
            vectorStoreService.indexChunks(entity, chunks);

            // 5. Update Entity status to INDEXED
            entity.setChunkCount(chunks.size());
            entity.setStatus(DocumentStatus.INDEXED);
            if (text.length() > 200) {
                entity.setSummary(text.substring(0, 197).replaceAll("\\s+", " ") + "...");
            } else {
                entity.setSummary(text);
            }
            entity = documentRepository.save(entity);

            log.info("Document successfully processed and indexed: ID={}", entity.getId());
            return toDto(entity);
        } catch (Exception e) {
            log.error("Failed to process document ID={}: {}", entity.getId(), e.getMessage(), e);
            entity.setStatus(DocumentStatus.FAILED);
            documentRepository.save(entity);
            throw new RuntimeException("Document processing failed: " + e.getMessage(), e);
        }
    }

    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAllByOrderByUploadedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DocumentDto getDocumentById(Long id) {
        DocumentEntity entity = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));
        return toDto(entity);
    }

    @Transactional
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new IllegalArgumentException("Document not found with ID: " + id);
        }
        documentRepository.deleteById(id);
        log.info("Deleted document ID: {}", id);
    }

    private DocumentDto toDto(DocumentEntity e) {
        return DocumentDto.builder()
                .id(e.getId())
                .fileName(e.getFileName())
                .contentType(e.getContentType())
                .fileSizeBytes(e.getFileSizeBytes())
                .status(e.getStatus())
                .chunkCount(e.getChunkCount())
                .summary(e.getSummary())
                .uploadedAt(e.getUploadedAt())
                .build();
    }
}
