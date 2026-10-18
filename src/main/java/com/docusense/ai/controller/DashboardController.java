package com.docusense.ai.controller;

import com.docusense.ai.dto.DashboardStatsDto;
import com.docusense.ai.entity.ChatMessageEntity;
import com.docusense.ai.entity.DocumentEntity;
import com.docusense.ai.entity.UserEntity;
import com.docusense.ai.repository.ChatMessageRepository;
import com.docusense.ai.repository.ChatSessionRepository;
import com.docusense.ai.repository.DocumentRepository;
import com.docusense.ai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DocumentRepository documentRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Value("${spring.ai.openai.api-key:unconfigured}")
    private String apiKey;

    public DashboardController(DocumentRepository documentRepository,
                               ChatSessionRepository chatSessionRepository,
                               ChatMessageRepository chatMessageRepository,
                               UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        boolean configured = apiKey != null && !apiKey.isBlank() && !apiKey.equalsIgnoreCase("unconfigured") && !apiKey.startsWith("demo_");

        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalDocuments(documentRepository.count())
                .indexedDocuments(documentRepository.countIndexedDocuments())
                .totalChunks(documentRepository.sumTotalChunks())
                .totalChatSessions(chatSessionRepository.count())
                .totalMessages(chatMessageRepository.count())
                .vectorStoreType("SimpleVectorStore / PGVector")
                .llmModel("Google Gemini 1.5 Flash / OpenAI")
                .apiConfigured(configured)
                .build();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/inspect-database")
    public ResponseEntity<Map<String, Object>> inspectDatabase() {
        Map<String, Object> data = new HashMap<>();
        List<UserEntity> users = userRepository.findAll();
        List<DocumentEntity> docs = documentRepository.findAll();
        List<ChatMessageEntity> messages = chatMessageRepository.findAll();

        data.put("totalUsersInDb", users.size());
        data.put("users", users);
        data.put("totalDocumentsInDb", docs.size());
        data.put("documents", docs);
        data.put("totalChatMessagesInDb", messages.size());
        data.put("recentMessages", messages.stream().limit(10).toList());

        return ResponseEntity.ok(data);
    }
}
