package com.docusense.ai.repository;

import com.docusense.ai.entity.DocumentEntity;
import com.docusense.ai.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByStatus(DocumentStatus status);
    List<DocumentEntity> findAllByOrderByUploadedAtDesc();

    @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.status = 'INDEXED'")
    long countIndexedDocuments();

    @Query("SELECT COALESCE(SUM(d.chunkCount), 0) FROM DocumentEntity d")
    long sumTotalChunks();
}
