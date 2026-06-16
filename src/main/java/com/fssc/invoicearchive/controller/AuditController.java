package com.fssc.invoicearchive.controller;

import com.fssc.invoicearchive.common.Result;
import com.fssc.invoicearchive.entity.AuditLog;
import com.fssc.invoicearchive.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/timeline/invoice/{invoiceId}")
    public Result<List<AuditLog>> getAuditTimelineByInvoiceId(@PathVariable Long invoiceId) {
        List<AuditLog> logs = auditLogService.getAuditTimelineByInvoiceId(invoiceId);
        return Result.success(logs);
    }

    @GetMapping("/timeline/code/{invoiceCode}")
    public Result<List<AuditLog>> getAuditTimelineByInvoiceCode(@PathVariable String invoiceCode) {
        List<AuditLog> logs = auditLogService.getAuditTimelineByInvoiceCode(invoiceCode);
        return Result.success(logs);
    }

    @GetMapping("/logs")
    public Result<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = auditLogService.getAllAuditLogs();
        return Result.success(logs);
    }
}
