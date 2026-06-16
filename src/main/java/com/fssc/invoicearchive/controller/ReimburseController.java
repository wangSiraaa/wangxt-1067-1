package com.fssc.invoicearchive.controller;

import com.fssc.invoicearchive.common.Result;
import com.fssc.invoicearchive.entity.Invoice;
import com.fssc.invoicearchive.entity.ReimburseBill;
import com.fssc.invoicearchive.service.ReimburseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reimburse")
@CrossOrigin(origins = "*")
public class ReimburseController {

    @Autowired
    private ReimburseService reimburseService;

    @PostMapping("/associate")
    public Result<Invoice> associateInvoice(@RequestBody Map<String, Object> params) {
        Long invoiceId = params.get("invoiceId") != null ? Long.valueOf(params.get("invoiceId").toString()) : null;
        Long reimburseBillId = params.get("reimburseBillId") != null ? Long.valueOf(params.get("reimburseBillId").toString()) : null;

        Invoice invoice = reimburseService.associateInvoice(invoiceId, reimburseBillId);
        return Result.success("关联成功", invoice);
    }

    @PostMapping("/disassociate")
    public Result<Invoice> disassociateInvoice(@RequestBody Map<String, Object> params) {
        Long invoiceId = params.get("invoiceId") != null ? Long.valueOf(params.get("invoiceId").toString()) : null;
        String reason = (String) params.get("reason");

        Invoice invoice = reimburseService.disassociateInvoice(invoiceId, reason);
        return Result.success("取消关联成功", invoice);
    }

    @PostMapping("/bill")
    public Result<ReimburseBill> createReimburseBill(@RequestBody ReimburseBill bill) {
        ReimburseBill saved = reimburseService.createReimburseBill(bill);
        return Result.success("创建成功", saved);
    }

    @GetMapping("/bill/{id}")
    public Result<ReimburseBill> getReimburseBill(@PathVariable Long id) {
        ReimburseBill bill = reimburseService.getReimburseBill(id);
        return Result.success(bill);
    }

    @GetMapping("/bill/no/{billNo}")
    public Result<ReimburseBill> getReimburseBillByNo(@PathVariable String billNo) {
        ReimburseBill bill = reimburseService.getReimburseBillByNo(billNo);
        return Result.success(bill);
    }

    @GetMapping("/bills")
    public Result<List<ReimburseBill>> getAllReimburseBills() {
        List<ReimburseBill> bills = reimburseService.getAllReimburseBills();
        return Result.success(bills);
    }
}
