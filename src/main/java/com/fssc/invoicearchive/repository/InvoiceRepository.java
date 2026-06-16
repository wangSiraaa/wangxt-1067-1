package com.fssc.invoicearchive.repository;

import com.fssc.invoicearchive.entity.Invoice;
import com.fssc.invoicearchive.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    boolean existsByInvoiceCode(String invoiceCode);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByUploadDept(String uploadDept);

    List<Invoice> findByReimburseBillId(Long reimburseBillId);

    List<Invoice> findByArchiveBatchId(Long archiveBatchId);

    long countByStatus(InvoiceStatus status);
}
