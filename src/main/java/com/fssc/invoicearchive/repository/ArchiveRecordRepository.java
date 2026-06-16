package com.fssc.invoicearchive.repository;

import com.fssc.invoicearchive.entity.ArchiveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveRecordRepository extends JpaRepository<ArchiveRecord, Long> {

    Optional<ArchiveRecord> findByArchiveNo(String archiveNo);

    Optional<ArchiveRecord> findByInvoiceId(Long invoiceId);

    List<ArchiveRecord> findByArchiveBatchId(Long archiveBatchId);

    List<ArchiveRecord> findByArchiveBoxNo(String archiveBoxNo);

    boolean existsByInvoiceId(Long invoiceId);
}
