package com.docusense.ai.controller;

import com.docusense.ai.dto.ChatDto;
import com.docusense.ai.entity.ChatMessageEntity;
import com.docusense.ai.entity.ChatSessionEntity;
import com.docusense.ai.repository.ChatMessageRepository;
import com.docusense.ai.repository.ChatSessionRepository;
import com.docusense.ai.service.RagChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final RagChatService ragChatService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatController(RagChatService ragChatService,
                          ChatSessionRepository chatSessionRepository,
                          ChatMessageRepository chatMessageRepository) {
        this.ragChatService = ragChatService;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatDto.AskResponse> askQuestion(@RequestBody ChatDto.AskRequest request) {
        ChatDto.AskResponse response = ragChatService.ask(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionEntity>> getSessions() {
        return ResponseEntity.ok(chatSessionRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<List<ChatDto.MessageDto>> getSessionMessages(@PathVariable Long id) {
        List<ChatMessageEntity> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(id);
        List<ChatDto.MessageDto> dtoList = messages.stream().map(m ->
                ChatDto.MessageDto.builder()
                        .id(m.getId())
                        .role(m.getRole())
                        .content(m.getContent())
                        .sources(m.getRetrievedSources())
                        .timestamp(m.getCreatedAt().toString())
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        chatSessionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Session deleted", "id", id));
    }

    /**
     * Server-Sent Events (SSE) streaming endpoint for real-time token streaming experience
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnswer(@RequestParam("question") String question,
                                   @RequestParam(value = "sessionId", required = false) Long sessionId,
                                   @RequestParam(value = "documentId", required = false) Long documentId) {
        SseEmitter emitter = new SseEmitter(60000L);

        CompletableFuture.runAsync(() -> {
            try {
                ChatDto.AskRequest req = ChatDto.AskRequest.builder()
                        .question(question)
                        .sessionId(sessionId)
                        .documentId(documentId)
                        .build();

                ChatDto.AskResponse response = ragChatService.ask(req);

                // Stream response in chunks for dynamic typing effect
                String[] words = response.getAnswer().split(" ");
                for (String word : words) {
                    emitter.send(SseEmitter.event().data(word + " "));
                    Thread.sleep(30);
                }

                emitter.send(SseEmitter.event().name("done").data(response));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
