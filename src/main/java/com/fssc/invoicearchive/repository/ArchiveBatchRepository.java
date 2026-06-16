package com.fssc.invoicearchive.repository;

import com.fssc.invoicearchive.entity.ArchiveBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchiveBatchRepository extends JpaRepository<ArchiveBatch, Long> {

    Optional<ArchiveBatch> findByBatchNo(String batchNo);

    boolean existsByBatchNo(String batchNo);
}
