package com.docusense.ai.dto;

import java.util.List;

public class ChatDto {

    public static class AskRequest {
        private String question;
        private Long sessionId;
        private Long documentId;
        private Integer topK;

        public AskRequest() {}

        public AskRequest(String question, Long sessionId, Long documentId, Integer topK) {
            this.question = question;
            this.sessionId = sessionId;
            this.documentId = documentId;
            this.topK = topK;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String question;
            private Long sessionId;
            private Long documentId;
            private Integer topK;

            public Builder question(String question) { this.question = question; return this; }
            public Builder sessionId(Long sessionId) { this.sessionId = sessionId; return this; }
            public Builder documentId(Long documentId) { this.documentId = documentId; return this; }
            public Builder topK(Integer topK) { this.topK = topK; return this; }

            public AskRequest build() {
                return new AskRequest(question, sessionId, documentId, topK);
            }
        }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }

        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }
    }

    public static class AskResponse {
        private String answer;
        private Long sessionId;
        private List<SourceCitation> sources;

        public AskResponse() {}

        public AskResponse(String answer, Long sessionId, List<SourceCitation> sources) {
            this.answer = answer;
            this.sessionId = sessionId;
            this.sources = sources;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String answer;
            private Long sessionId;
            private List<SourceCitation> sources;

            public Builder answer(String answer) { this.answer = answer; return this; }
            public Builder sessionId(Long sessionId) { this.sessionId = sessionId; return this; }
            public Builder sources(List<SourceCitation> sources) { this.sources = sources; return this; }

            public AskResponse build() {
                return new AskResponse(answer, sessionId, sources);
            }
        }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public List<SourceCitation> getSources() { return sources; }
        public void setSources(List<SourceCitation> sources) { this.sources = sources; }
    }

    public static class SourceCitation {
        private String documentName;
        private Long documentId;
        private Integer chunkIndex;
        private String snippet;
        private Double score;

        public SourceCitation() {}

        public SourceCitation(String documentName, Long documentId, Integer chunkIndex, String snippet, Double score) {
            this.documentName = documentName;
            this.documentId = documentId;
            this.chunkIndex = chunkIndex;
            this.snippet = snippet;
            this.score = score;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String documentName;
            private Long documentId;
            private Integer chunkIndex;
            private String snippet;
            private Double score;

            public Builder documentName(String documentName) { this.documentName = documentName; return this; }
            public Builder documentId(Long documentId) { this.documentId = documentId; return this; }
            public Builder chunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; return this; }
            public Builder snippet(String snippet) { this.snippet = snippet; return this; }
            public Builder score(Double score) { this.score = score; return this; }

            public SourceCitation build() {
                return new SourceCitation(documentName, documentId, chunkIndex, snippet, score);
            }
        }

        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }

        public Integer getChunkIndex() { return chunkIndex; }
        public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }

    public static class MessageDto {
        private Long id;
        private String role;
        private String content;
        private String sources;
        private String timestamp;

        public MessageDto() {}

        public MessageDto(Long id, String role, String content, String sources, String timestamp) {
            this.id = id;
            this.role = role;
            this.content = content;
            this.sources = sources;
            this.timestamp = timestamp;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long id;
            private String role;
            private String content;
            private String sources;
            private String timestamp;

            public Builder id(Long id) { this.id = id; return this; }
            public Builder role(String role) { this.role = role; return this; }
            public Builder content(String content) { this.content = content; return this; }
            public Builder sources(String sources) { this.sources = sources; return this; }
            public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }

            public MessageDto build() {
                return new MessageDto(id, role, content, sources, timestamp);
            }
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getSources() { return sources; }
        public void setSources(String sources) { this.sources = sources; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
