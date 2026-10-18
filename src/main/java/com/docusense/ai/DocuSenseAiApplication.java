package com.docusense.ai;

import com.docusense.ai.entity.DocumentEntity;
import com.docusense.ai.entity.DocumentStatus;
import com.docusense.ai.entity.UserEntity;
import com.docusense.ai.repository.DocumentRepository;
import com.docusense.ai.repository.UserRepository;
import com.docusense.ai.service.PdfProcessingService;
import com.docusense.ai.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(exclude = {OpenAiAutoConfiguration.class})
public class DocuSenseAiApplication {

    private static final Logger log = LoggerFactory.getLogger(DocuSenseAiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DocuSenseAiApplication.class, args);
        log.info("=========================================================");
        log.info("  DocuSense AI Started Successfully!                     ");
        log.info("  Web UI:       http://localhost:8080/                   ");
        log.info("  H2 Console:   http://localhost:8080/h2-console         ");
        log.info("=========================================================");
    }

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          DocumentRepository documentRepository,
                                          PdfProcessingService pdfProcessingService,
                                          VectorStoreService vectorStoreService) {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Initializing demo seed data (User & Sample Document)...");

                UserEntity user = UserEntity.builder()
                        .email("alekhya@example.com")
                        .fullName("Budati Alekhya")
                        .role("ROLE_DEVELOPER")
                        .build();
                user = userRepository.save(user);

                // Create a sample enterprise document in DB
                String sampleText = """
                        Acme Corporation - Employee Leave and Remote Work Policy (2026):
                        1. Paid Time Off (PTO): Every full-time employee is entitled to 24 days of paid time off per calendar year.
                        2. Sick Leave: Employees receive 12 dedicated sick leave days per year. A medical certificate is required for absences exceeding 3 consecutive days.
                        3. Remote Work & Hybrid Schedule: Employees can work remotely up to 3 days per week with manager approval. Core office hours are 10:00 AM to 4:00 PM IST.
                        4. Health and Wellness Allowance: An annual reimbursement of up to Rs. 25,000 is available for gym memberships, health insurance, and ergonomics.
                        5. Notice Period: Standard notice period for resignation is 60 days for software engineering roles.
                        """;

                DocumentEntity sampleDoc = DocumentEntity.builder()
                        .fileName("Acme_Corp_Employee_Policy.pdf")
                        .contentType("application/pdf")
                        .fileSizeBytes((long) sampleText.length())
                        .status(DocumentStatus.PROCESSING)
                        .summary("Employee Leave and Remote Work Policy covering PTO, Sick Leave, and Hybrid Work.")
                        .user(user)
                        .build();
                sampleDoc = documentRepository.save(sampleDoc);

                List<PdfProcessingService.DocumentChunk> chunks = pdfProcessingService.chunkText(sampleText);
                vectorStoreService.indexChunks(sampleDoc, chunks);

                sampleDoc.setStatus(DocumentStatus.INDEXED);
                sampleDoc.setChunkCount(chunks.size());
                documentRepository.save(sampleDoc);

                log.info("Demo document '{}' indexed successfully with {} chunks!", sampleDoc.getFileName(), chunks.size());
            }
        };
    }
}
