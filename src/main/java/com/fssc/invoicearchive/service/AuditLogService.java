package com.fssc.invoicearchive.service;

import com.fssc.invoicearchive.entity.AuditLog;
import com.fssc.invoicearchive.entity.InvoiceStatus;
import com.fssc.invoicearchive.entity.RoleType;
import com.fssc.invoicearchive.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logOperation(Long invoiceId, String invoiceCode, String operationType,
                             String operationDesc, InvoiceStatus beforeStatus, InvoiceStatus afterStatus,
                             Long operatorId, String operatorName, RoleType operatorRole) {
        AuditLog log = new AuditLog();
        log.setInvoiceId(invoiceId);
        log.setInvoiceCode(invoiceCode);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setBeforeStatus(beforeStatus != null ? beforeStatus.name() : null);
        log.setAfterStatus(afterStatus != null ? afterStatus.name() : null);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperatorRole(operatorRole != null ? operatorRole.name() : null);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditTimelineByInvoiceId(Long invoiceId) {
        return auditLogRepository.findByInvoiceIdOrderByCreateTimeDesc(invoiceId);
    }

    public List<AuditLog> getAuditTimelineByInvoiceCode(String invoiceCode) {
        return auditLogRepository.findByInvoiceCodeOrderByCreateTimeDesc(invoiceCode);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
