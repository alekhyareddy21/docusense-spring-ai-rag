package com.docusense.ai.service;

import com.docusense.ai.dto.ChatDto;
import com.docusense.ai.entity.DocumentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void indexChunks(DocumentEntity docEntity, List<PdfProcessingService.DocumentChunk> chunks) {
        List<Document> aiDocs = new ArrayList<>();

        for (PdfProcessingService.DocumentChunk chunk : chunks) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", docEntity.getId());
            metadata.put("documentName", docEntity.getFileName());
            metadata.put("chunkIndex", chunk.index());

            Document aiDoc = new Document(chunk.text(), metadata);
            aiDocs.add(aiDoc);
        }

        log.info("Indexing {} vector embeddings for document: {} (ID: {})",
                aiDocs.size(), docEntity.getFileName(), docEntity.getId());
        vectorStore.add(aiDocs);
        log.info("Successfully indexed vector embeddings.");
    }

    public List<ChatDto.SourceCitation> searchSimilar(String query, int topK, Long documentIdFilter) {
        int targetK = Math.max(topK, 4);
        int fetchK = (documentIdFilter != null) ? Math.max(targetK * 15, 60) : targetK;
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(fetchK);

        List<Document> similarDocs;
        try {
            similarDocs = vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.error("Error executing vector similarity search: {}", e.getMessage());
            return List.of();
        }

        if (documentIdFilter != null) {
            similarDocs = similarDocs.stream()
                    .filter(doc -> {
                        Object docIdObj = doc.getMetadata().get("documentId");
                        if (docIdObj == null) return false;
                        return documentIdFilter.toString().equals(docIdObj.toString());
                    })
                    .limit(targetK)
                    .collect(Collectors.toList());
        }

        return similarDocs.stream().map(doc -> {
            Map<String, Object> meta = doc.getMetadata();
            String docName = meta.getOrDefault("documentName", "Unknown Document").toString();
            Long docId = meta.containsKey("documentId") ? Long.valueOf(meta.get("documentId").toString()) : null;
            Integer chunkIdx = meta.containsKey("chunkIndex") ? Integer.valueOf(meta.get("chunkIndex").toString()) : 0;
            Double score = null;
            if (meta.containsKey("distance")) {
                score = Double.valueOf(meta.get("distance").toString());
            }

            return ChatDto.SourceCitation.builder()
                    .documentName(docName)
                    .documentId(docId)
                    .chunkIndex(chunkIdx)
                    .snippet(doc.getContent())
                    .score(score)
                    .build();
        }).collect(Collectors.toList());
    }
}
