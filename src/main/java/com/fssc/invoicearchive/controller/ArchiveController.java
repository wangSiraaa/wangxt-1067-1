package com.fssc.invoicearchive.controller;

import com.fssc.invoicearchive.common.Result;
import com.fssc.invoicearchive.entity.ArchiveBatch;
import com.fssc.invoicearchive.entity.ArchiveRecord;
import com.fssc.invoicearchive.entity.Invoice;
import com.fssc.invoicearchive.entity.UnsealRequest;
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

    @PostMapping("/batch/spotcheck")
    public Result<ArchiveBatch> spotCheckBatch(@RequestBody Map<String, Object> params) {
        Long batchId = params.get("batchId") != null ? Long.valueOf(params.get("batchId").toString()) : null;
        String reason = (String) params.get("reason");

        ArchiveBatch batch = archiveService.spotCheckBatch(batchId, reason);
        return Result.success("审计抽查已发起", batch);
    }

    @PostMapping("/batch/endSpotcheck")
    public Result<ArchiveBatch> endSpotCheck(@RequestBody Map<String, Object> params) {
        Long batchId = params.get("batchId") != null ? Long.valueOf(params.get("batchId").toString()) : null;

        ArchiveBatch batch = archiveService.endSpotCheck(batchId);
        return Result.success("审计抽查已结束", batch);
    }

    @PostMapping("/unseal/submit")
    public Result<UnsealRequest> submitUnsealRequest(@RequestBody Map<String, Object> params) {
        Long batchId = params.get("batchId") != null ? Long.valueOf(params.get("batchId").toString()) : null;
        String requestType = (String) params.get("requestType");
        String reason = (String) params.get("reason");

        UnsealRequest request = archiveService.submitUnsealRequest(batchId, requestType, reason);
        return Result.success("解封申请已提交", request);
    }

    @PostMapping("/unseal/approve")
    public Result<UnsealRequest> approveUnsealRequest(@RequestBody Map<String, Object> params) {
        Long requestId = params.get("requestId") != null ? Long.valueOf(params.get("requestId").toString()) : null;

        UnsealRequest request = archiveService.approveUnsealRequest(requestId);
        return Result.success("解封申请已审批通过", request);
    }

    @PostMapping("/unseal/reject")
    public Result<UnsealRequest> rejectUnsealRequest(@RequestBody Map<String, Object> params) {
        Long requestId = params.get("requestId") != null ? Long.valueOf(params.get("requestId").toString()) : null;
        String rejectReason = (String) params.get("rejectReason");

        UnsealRequest request = archiveService.rejectUnsealRequest(requestId, rejectReason);
        return Result.success("解封申请已驳回", request);
    }

    @GetMapping("/unseal/requests")
    public Result<List<UnsealRequest>> getAllUnsealRequests() {
        List<UnsealRequest> requests = archiveService.getAllUnsealRequests();
        return Result.success(requests);
    }

    @GetMapping("/unseal/pending")
    public Result<List<UnsealRequest>> getPendingUnsealRequests() {
        List<UnsealRequest> requests = archiveService.getPendingUnsealRequests();
        return Result.success(requests);
    }

    @GetMapping("/unseal/batch/{batchId}")
    public Result<List<UnsealRequest>> getUnsealRequestsByBatch(@PathVariable Long batchId) {
        List<UnsealRequest> requests = archiveService.getUnsealRequestsByBatch(batchId);
        return Result.success(requests);
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
