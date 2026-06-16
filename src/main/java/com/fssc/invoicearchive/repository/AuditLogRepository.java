package com.fssc.invoicearchive.repository;

import com.fssc.invoicearchive.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByInvoiceIdOrderByCreateTimeDesc(Long invoiceId);

    List<AuditLog> findByInvoiceCodeOrderByCreateTimeDesc(String invoiceCode);

    List<AuditLog> findByOperatorIdOrderByCreateTimeDesc(Long operatorId);

    List<AuditLog> findByOperationTypeOrderByCreateTimeDesc(String operationType);
}
