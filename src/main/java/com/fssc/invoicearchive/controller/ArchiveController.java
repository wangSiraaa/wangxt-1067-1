package com.fssc.invoicearchive.controller;

import com.fssc.invoicearchive.common.Result;
import com.fssc.invoicearchive.entity.ArchiveBatch;
import com.fssc.invoicearchive.entity.ArchiveRecord;
import com.fssc.invoicearchive.entity.Invoice;
import com.fssc.invoicearchive.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/archive")
@CrossOrigin(origins = "*")
public class ArchiveController {

    @Autowired
    private ArchiveService archiveService;

    @PostMapping("/archive")
    public Result<ArchiveRecord> archiveInvoice(@RequestBody Map<String, Object> params) {
        Long invoiceId = params.get("invoiceId") != null ? Long.valueOf(params.get("invoiceId").toString()) : null;
        String archiveBoxNo = (String) params.get("archiveBoxNo");
        String archivePosition = (String) params.get("archivePosition");
        Long archiveBatchId = params.get("archiveBatchId") != null ? Long.valueOf(params.get("archiveBatchId").toString()) : null;

        ArchiveRecord record = archiveService.archiveInvoice(invoiceId, archiveBoxNo, archivePosition, archiveBatchId);
        return Result.success("归档成功", record);
    }

    @PostMapping("/return")
    public Result<Invoice> returnInvoice(@RequestBody Map<String, Object> params) {
        Long invoiceId = params.get("invoiceId") != null ? Long.valueOf(params.get("invoiceId").toString()) : null;
        String returnReason = (String) params.get("returnReason");

        Invoice invoice = archiveService.returnInvoice(invoiceId, returnReason);
        return Result.success("退回成功", invoice);
    }

    @PostMapping("/batch")
    public Result<ArchiveBatch> createBatch(@RequestBody Map<String, Object> params) {
        String batchName = (String) params.get("batchName");
        String remark = (String) params.get("remark");

        ArchiveBatch batch = archiveService.createBatch(batchName, remark);
        return Result.success("批次创建成功", batch);
    }

    @PostMapping("/batch/seal")
    public Result<ArchiveBatch> sealBatch(@RequestBody Map<String, Object> params) {
        Long batchId = params.get("batchId") != null ? Long.valueOf(params.get("batchId").toString()) : null;

        ArchiveBatch batch = archiveService.sealBatch(batchId);
        return Result.success("批次封存成功", batch);
    }

    @GetMapping("/batches")
    public Result<List<ArchiveBatch>> getAllBatches() {
        List<ArchiveBatch> batches = archiveService.getAllBatches();
        return Result.success(batches);
    }

    @GetMapping("/batch/{id}")
    public Result<ArchiveBatch> getBatchById(@PathVariable Long id) {
        ArchiveBatch batch = archiveService.getBatchById(id);
        return Result.success(batch);
    }

    @GetMapping("/batch/{id}/invoices")
    public Result<List<Invoice>> getInvoicesByBatch(@PathVariable Long id) {
        List<Invoice> invoices = archiveService.getInvoicesByBatch(id);
        return Result.success(invoices);
    }

    @GetMapping("/records")
    public Result<List<ArchiveRecord>> getArchiveRecords() {
        List<ArchiveRecord> records = archiveService.getArchiveRecords();
        return Result.success(records);
    }

    @GetMapping("/record/{id}")
    public Result<ArchiveRecord> getArchiveRecord(@PathVariable Long id) {
        ArchiveRecord record = archiveService.getArchiveRecord(id);
        return Result.success(record);
    }

    @GetMapping("/batch/{id}/records")
    public Result<List<ArchiveRecord>> getArchiveRecordsByBatch(@PathVariable Long id) {
        List<ArchiveRecord> records = archiveService.getArchiveRecordsByBatch(id);
        return Result.success(records);
    }
}
