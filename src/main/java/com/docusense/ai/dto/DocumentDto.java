package com.docusense.ai.dto;

import com.docusense.ai.entity.DocumentStatus;
import java.time.LocalDateTime;

public class DocumentDto {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSizeBytes;
    private DocumentStatus status;
    private Integer chunkCount;
    private String summary;
    private LocalDateTime uploadedAt;

    public DocumentDto() {}

    public DocumentDto(Long id, String fileName, String contentType, Long fileSizeBytes,
                       DocumentStatus status, Integer chunkCount, String summary,
                       LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.status = status;
        this.chunkCount = chunkCount;
        this.summary = summary;
        this.uploadedAt = uploadedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String fileName;
        private String contentType;
        private Long fileSizeBytes;
        private DocumentStatus status;
        private Integer chunkCount;
        private String summary;
        private LocalDateTime uploadedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder fileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }
        public Builder status(DocumentStatus status) { this.status = status; return this; }
        public Builder chunkCount(Integer chunkCount) { this.chunkCount = chunkCount; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public DocumentDto build() {
            return new DocumentDto(id, fileName, contentType, fileSizeBytes, status, chunkCount, summary, uploadedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
