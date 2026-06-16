package com.fssc.invoicearchive.service;

import com.fssc.invoicearchive.common.BusinessException;
import com.fssc.invoicearchive.context.UserContext;
import com.fssc.invoicearchive.entity.*;
import com.fssc.invoicearchive.repository.ArchiveBatchRepository;
import com.fssc.invoicearchive.repository.ArchiveRecordRepository;
import com.fssc.invoicearchive.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ArchiveService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ArchiveRecordRepository archiveRecordRepository;

    @Autowired
    private ArchiveBatchRepository archiveBatchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public ArchiveRecord archiveInvoice(Long invoiceId, String archiveBoxNo, String archivePosition, Long archiveBatchId) {

        userService.checkRole(RoleType.ARCHIVIST);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("票据不存在"));

        if (invoice.getStatus() != InvoiceStatus.ASSOCIATED) {
            throw new BusinessException("只有已关联的票据才能归档，当前状态: " + invoice.getStatus());
        }

        if (invoice.getReimburseBillId() == null) {
            throw new BusinessException("未关联业务单据不能归档");
        }

        if (archiveRecordRepository.existsByInvoiceId(invoiceId)) {
            throw new BusinessException("该票据已归档，不能重复归档");
        }

        if (!invoiceRepository.existsByInvoiceCode(invoice.getInvoiceCode())) {
            throw new BusinessException("票据代码不存在");
        }

        if (archiveBoxNo == null || archiveBoxNo.trim().isEmpty()) {
            throw new BusinessException("归档盒位置不能为空");
        }

        if (invoice.getImageUrl() == null || invoice.getImageUrl().trim().isEmpty()) {
            throw new BusinessException("票据影像不完整，不能归档");
        }

        if (archiveBatchId != null) {
            ArchiveBatch batch = archiveBatchRepository.findById(archiveBatchId)
                    .orElseThrow(() -> new BusinessException("归档批次不存在"));
            if (batch.getSealed()) {
                throw new BusinessException("归档批次已封存，不能新增票据");
            }
        }

        String archiveNo = generateArchiveNo();

        ArchiveRecord record = new ArchiveRecord();
        record.setArchiveNo(archiveNo);
        record.setInvoiceId(invoiceId);
        record.setInvoiceCode(invoice.getInvoiceCode());
        record.setArchiveBoxNo(archiveBoxNo);
        record.setArchivePosition(archivePosition);
        record.setArchivistId(UserContext.getCurrentUserId());
        record.setArchivistName(UserContext.getCurrentUserName());
        record.setArchiveBatchId(archiveBatchId);
        record.setStatus("NORMAL");
        record = archiveRecordRepository.save(record);

        InvoiceStatus beforeStatus = invoice.getStatus();

        invoice.setStatus(InvoiceStatus.ARCHIVED);
        invoice.setArchiveRecordId(record.getId());
        invoice.setArchiveBoxNo(archiveBoxNo);
        invoice.setArchivePosition(archivePosition);
        invoice.setArchiveTime(LocalDateTime.now());
        invoice.setArchivistId(UserContext.getCurrentUserId());
        invoice.setArchivistName(UserContext.getCurrentUserName());
        invoice.setArchiveBatchId(archiveBatchId);
        invoiceRepository.save(invoice);

        if (archiveBatchId != null) {
            ArchiveBatch batch = archiveBatchRepository.findById(archiveBatchId).get();
            batch.setInvoiceCount(batch.getInvoiceCount() + 1);
            archiveBatchRepository.save(batch);
        }

        auditLogService.logOperation(
                invoiceId,
                invoice.getInvoiceCode(),
                "ARCHIVE",
                "归档入册，归档盒: " + archiveBoxNo + "，位置: " + archivePosition,
                beforeStatus,
                InvoiceStatus.ARCHIVED,
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.ARCHIVIST
        );

        return record;
    }

    @Transactional
    public Invoice returnInvoice(Long invoiceId, String returnReason) {

        userService.checkRole(RoleType.ARCHIVIST);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("票据不存在"));

        if (invoice.getStatus() != InvoiceStatus.ARCHIVED && invoice.getStatus() != InvoiceStatus.ASSOCIATED) {
            throw new BusinessException("当前状态不能退回，状态: " + invoice.getStatus());
        }

        if (invoice.getArchiveBatchId() != null) {
            ArchiveBatch batch = archiveBatchRepository.findById(invoice.getArchiveBatchId()).orElse(null);
            if (batch != null && batch.getSealed()) {
                throw new BusinessException("归档批次已封存，不能退回");
            }
        }

        InvoiceStatus beforeStatus = invoice.getStatus();

        if (invoice.getArchiveRecordId() != null) {
            archiveRecordRepository.deleteById(invoice.getArchiveRecordId());
        }

        invoice.setStatus(InvoiceStatus.RETURNED);
        invoice.setReturnReason(returnReason);
        invoice.setReturnTime(LocalDateTime.now());
        invoice.setReturnOperatorId(UserContext.getCurrentUserId());
        invoice.setReturnOperatorName(UserContext.getCurrentUserName());
        invoice.setArchiveRecordId(null);
        invoice.setArchiveBoxNo(null);
        invoice.setArchivePosition(null);
        invoice.setArchiveTime(null);
        invoice.setArchivistId(null);
        invoice.setArchivistName(null);

        if (invoice.getArchiveBatchId() != null) {
            ArchiveBatch batch = archiveBatchRepository.findById(invoice.getArchiveBatchId()).get();
            batch.setInvoiceCount(Math.max(0, batch.getInvoiceCount() - 1));
            archiveBatchRepository.save(batch);
            invoice.setArchiveBatchId(null);
        }

        invoice = invoiceRepository.save(invoice);

        auditLogService.logOperation(
                invoiceId,
                invoice.getInvoiceCode(),
                "RETURN",
                "退回补扫，原因: " + returnReason,
                beforeStatus,
                InvoiceStatus.RETURNED,
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.ARCHIVIST
        );

        return invoice;
    }

    @Transactional
    public ArchiveBatch createBatch(String batchName, String remark) {
        userService.checkRole(RoleType.ARCHIVIST);

        String batchNo = generateBatchNo();

        ArchiveBatch batch = new ArchiveBatch();
        batch.setBatchNo(batchNo);
        batch.setBatchName(batchName);
        batch.setInvoiceCount(0);
        batch.setArchivistId(UserContext.getCurrentUserId());
        batch.setArchivistName(UserContext.getCurrentUserName());
        batch.setSealed(false);
        batch.setRemark(remark);

        return archiveBatchRepository.save(batch);
    }

    @Transactional
    public ArchiveBatch sealBatch(Long batchId) {
        userService.checkRole(RoleType.ARCHIVIST);

        ArchiveBatch batch = archiveBatchRepository.findById(batchId)
                .orElseThrow(() -> new BusinessException("归档批次不存在"));

        if (batch.getSealed()) {
            throw new BusinessException("批次已封存，不能重复封存");
        }

        batch.setSealed(true);
        batch.setSealTime(LocalDateTime.now());
        batch.setSealOperatorId(UserContext.getCurrentUserId());
        batch.setSealOperatorName(UserContext.getCurrentUserName());
        batch = archiveBatchRepository.save(batch);

        List<Invoice> invoices = invoiceRepository.findByArchiveBatchId(batchId);
        for (Invoice invoice : invoices) {
            InvoiceStatus beforeStatus = invoice.getStatus();
            invoice.setStatus(InvoiceStatus.SEALED);
            invoiceRepository.save(invoice);

            auditLogService.logOperation(
                    invoice.getId(),
                    invoice.getInvoiceCode(),
                    "SEAL",
                    "批次封存，批次号: " + batch.getBatchNo(),
                    beforeStatus,
                    InvoiceStatus.SEALED,
                    UserContext.getCurrentUserId(),
                    UserContext.getCurrentUserName(),
                    RoleType.ARCHIVIST
            );
        }

        return batch;
    }

    public List<ArchiveBatch> getAllBatches() {
        return archiveBatchRepository.findAll();
    }

    public ArchiveBatch getBatchById(Long id) {
        return archiveBatchRepository.findById(id)
                .orElseThrow(() -> new BusinessException("归档批次不存在"));
    }

    public List<ArchiveRecord> getArchiveRecords() {
        return archiveRecordRepository.findAll();
    }

    public ArchiveRecord getArchiveRecord(Long id) {
        return archiveRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("归档记录不存在"));
    }

    public List<ArchiveRecord> getArchiveRecordsByBatch(Long batchId) {
        return archiveRecordRepository.findByArchiveBatchId(batchId);
    }

    public List<Invoice> getInvoicesByBatch(Long batchId) {
        return invoiceRepository.findByArchiveBatchId(batchId);
    }

    private String generateArchiveNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = archiveRecordRepository.count();
        return "GD" + dateStr + String.format("%04d", count + 1);
    }

    private String generateBatchNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = archiveBatchRepository.count();
        return "PC" + dateStr + String.format("%04d", count + 1);
    }
}
