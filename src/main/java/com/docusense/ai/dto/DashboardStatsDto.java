package com.docusense.ai.dto;

public class DashboardStatsDto {
    private long totalDocuments;
    private long indexedDocuments;
    private long totalChunks;
    private long totalChatSessions;
    private long totalMessages;
    private String vectorStoreType;
    private String llmModel;
    private boolean apiConfigured;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long totalDocuments, long indexedDocuments, long totalChunks,
                             long totalChatSessions, long totalMessages, String vectorStoreType,
                             String llmModel, boolean apiConfigured) {
        this.totalDocuments = totalDocuments;
        this.indexedDocuments = indexedDocuments;
        this.totalChunks = totalChunks;
        this.totalChatSessions = totalChatSessions;
        this.totalMessages = totalMessages;
        this.vectorStoreType = vectorStoreType;
        this.llmModel = llmModel;
        this.apiConfigured = apiConfigured;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalDocuments;
        private long indexedDocuments;
        private long totalChunks;
        private long totalChatSessions;
        private long totalMessages;
        private String vectorStoreType;
        private String llmModel;
        private boolean apiConfigured;

        public Builder totalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; return this; }
        public Builder indexedDocuments(long indexedDocuments) { this.indexedDocuments = indexedDocuments; return this; }
        public Builder totalChunks(long totalChunks) { this.totalChunks = totalChunks; return this; }
        public Builder totalChatSessions(long totalChatSessions) { this.totalChatSessions = totalChatSessions; return this; }
        public Builder totalMessages(long totalMessages) { this.totalMessages = totalMessages; return this; }
        public Builder vectorStoreType(String vectorStoreType) { this.vectorStoreType = vectorStoreType; return this; }
        public Builder llmModel(String llmModel) { this.llmModel = llmModel; return this; }
        public Builder apiConfigured(boolean apiConfigured) { this.apiConfigured = apiConfigured; return this; }

        public DashboardStatsDto build() {
            return new DashboardStatsDto(totalDocuments, indexedDocuments, totalChunks,
                    totalChatSessions, totalMessages, vectorStoreType, llmModel, apiConfigured);
        }
    }

    public long getTotalDocuments() { return totalDocuments; }
    public void setTotalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; }

    public long getIndexedDocuments() { return indexedDocuments; }
    public void setIndexedDocuments(long indexedDocuments) { this.indexedDocuments = indexedDocuments; }

    public long getTotalChunks() { return totalChunks; }
    public void setTotalChunks(long totalChunks) { this.totalChunks = totalChunks; }

    public long getTotalChatSessions() { return totalChatSessions; }
    public void setTotalChatSessions(long totalChatSessions) { this.totalChatSessions = totalChatSessions; }

    public long getTotalMessages() { return totalMessages; }
    public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }

    public String getVectorStoreType() { return vectorStoreType; }
    public void setVectorStoreType(String vectorStoreType) { this.vectorStoreType = vectorStoreType; }

    public String getLlmModel() { return llmModel; }
    public void setLlmModel(String llmModel) { this.llmModel = llmModel; }

    public boolean isApiConfigured() { return apiConfigured; }
    public void setApiConfigured(boolean apiConfigured) { this.apiConfigured = apiConfigured; }
}
