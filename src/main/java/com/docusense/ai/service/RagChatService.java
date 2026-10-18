package com.docusense.ai.service;

import com.docusense.ai.dto.ChatDto;
import com.docusense.ai.entity.ChatMessageEntity;
import com.docusense.ai.entity.ChatSessionEntity;
import com.docusense.ai.repository.ChatMessageRepository;
import com.docusense.ai.repository.ChatSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);

    private final VectorStoreService vectorStoreService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;

    public RagChatService(VectorStoreService vectorStoreService,
                          ChatSessionRepository chatSessionRepository,
                          ChatMessageRepository chatMessageRepository,
                          @Autowired(required = false) ChatModel chatModel) {
        this.vectorStoreService = vectorStoreService;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatModel;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public ChatDto.AskResponse ask(ChatDto.AskRequest request) {
        String question = request.getQuestion();
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be blank");
        }

        // 1. Resolve or create Chat Session in DB via Hibernate
        ChatSessionEntity session = resolveSession(request.getSessionId(), question);

        // 2. Persist USER message
        ChatMessageEntity userMsg = ChatMessageEntity.builder()
                .role("USER")
                .content(question)
                .session(session)
                .build();
        chatMessageRepository.save(userMsg);

        // 3. Retrieval: Query Vector Store for top-k relevant document chunks
        int topK = (request.getTopK() != null && request.getTopK() > 0) ? request.getTopK() : 4;
        List<ChatDto.SourceCitation> sources = vectorStoreService.searchSimilar(question, topK, request.getDocumentId());
        log.info("RAG Retrieval retrieved {} candidate chunks for question: '{}'", sources.size(), question);

        // 4. Augmentation: Build context-enhanced prompt
        String augmentedPrompt = buildAugmentedPrompt(question, sources);

        // 5. Generation: Call LLM (or dynamic simulated fallback if offline/no key)
        String answer;
        try {
            if (chatModel != null) {
                log.info("Sending augmented prompt to LLM ChatModel...");
                org.springframework.ai.chat.model.ChatResponse response = chatModel.call(new Prompt(augmentedPrompt));
                answer = response.getResult().getOutput().getContent();
            } else {
                answer = generateLocalRAGAnswer(question, sources);
            }
        } catch (Exception e) {
            log.warn("LLM API call failed or is unconfigured ({}). Falling back to local RAG response generator.", e.getMessage());
            answer = generateLocalRAGAnswer(question, sources);
        }

        // 6. Serialize sources metadata and persist ASSISTANT message via Hibernate
        String sourcesJson = null;
        try {
            sourcesJson = objectMapper.writeValueAsString(sources);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize sources to JSON", e);
        }

        ChatMessageEntity assistantMsg = ChatMessageEntity.builder()
                .role("ASSISTANT")
                .content(answer)
                .retrievedSources(sourcesJson)
                .session(session)
                .build();
        chatMessageRepository.save(assistantMsg);

        return ChatDto.AskResponse.builder()
                .answer(answer)
                .sessionId(session.getId())
                .sources(sources)
                .build();
    }

    private ChatSessionEntity resolveSession(Long sessionId, String firstQuestion) {
        if (sessionId != null) {
            return chatSessionRepository.findById(sessionId).orElseGet(() -> createNewSession(firstQuestion));
        }
        return createNewSession(firstQuestion);
    }

    private ChatSessionEntity createNewSession(String question) {
        String title = question.length() > 40 ? question.substring(0, 37) + "..." : question;
        ChatSessionEntity newSession = ChatSessionEntity.builder()
                .title(title)
                .build();
        return chatSessionRepository.save(newSession);
    }

    private String buildAugmentedPrompt(String question, List<ChatDto.SourceCitation> sources) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are DocuSense AI, an enterprise document intelligence and Q&A assistant.\n");
        sb.append("Answer the user's question clearly and accurately using ONLY the context provided below.\n");
        sb.append("If the context does not contain enough information to answer, state that honestly without guessing.\n\n");
        sb.append("--- CONTEXT FROM UPLOADED DOCUMENTS ---\n");

        if (sources.isEmpty()) {
            sb.append("(No directly matching document context found. Please ask based on general knowledge or instruct user to upload documents.)\n");
        } else {
            for (int i = 0; i < sources.size(); i++) {
                ChatDto.SourceCitation src = sources.get(i);
                sb.append(String.format("[Source %d - %s (Chunk #%d)]:\n%s\n\n",
                        i + 1, src.getDocumentName(), src.getChunkIndex(), src.getSnippet()));
            }
        }

        sb.append("--- USER QUESTION ---\n");
        sb.append(question).append("\n\n");
        sb.append("--- ANSWER ---");
        return sb.toString();
    }

    private String generateLocalRAGAnswer(String question, List<ChatDto.SourceCitation> sources) {
        if (sources.isEmpty()) {
            return "No relevant information found in the indexed documents for your query: \"" + question + "\". Please upload a document or refine your search keywords.";
        }

        StringBuilder response = new StringBuilder();
        response.append("Based on the indexed document context:\n\n");
        for (int i = 0; i < Math.min(sources.size(), 2); i++) {
            ChatDto.SourceCitation s = sources.get(i);
            response.append("• **From [").append(s.getDocumentName()).append("]:**\n");
            response.append("> ").append(s.getSnippet().trim()).append("\n\n");
        }
        response.append("*(Note: To enable full generative AI synthesis, provide your Google Gemini / OpenAI API Key in application.yml or via the Settings tab.)*");
        return response.toString();
    }
}
