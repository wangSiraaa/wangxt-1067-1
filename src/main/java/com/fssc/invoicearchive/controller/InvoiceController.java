package com.fssc.invoicearchive.controller;

import com.fssc.invoicearchive.common.Result;
import com.fssc.invoicearchive.entity.Invoice;
import com.fssc.invoicearchive.entity.InvoiceImageVersion;
import com.fssc.invoicearchive.entity.InvoiceStatus;
import com.fssc.invoicearchive.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoice")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/upload")
    public Result<Invoice> uploadInvoice(@RequestBody Map<String, Object> params) {
        String invoiceCode = (String) params.get("invoiceCode");
        String invoiceNumber = (String) params.get("invoiceNumber");
        String invoiceDateStr = (String) params.get("invoiceDate");
        LocalDate invoiceDate = invoiceDateStr != null ? LocalDate.parse(invoiceDateStr) : null;
        BigDecimal amount = params.get("amount") != null ? new BigDecimal(params.get("amount").toString()) : null;
        BigDecimal taxAmount = params.get("taxAmount") != null ? new BigDecimal(params.get("taxAmount").toString()) : null;
        BigDecimal totalAmount = params.get("totalAmount") != null ? new BigDecimal(params.get("totalAmount").toString()) : null;
        String sellerName = (String) params.get("sellerName");
        String sellerTaxNumber = (String) params.get("sellerTaxNumber");
        String buyerName = (String) params.get("buyerName");
        String buyerTaxNumber = (String) params.get("buyerTaxNumber");
        String imageUrl = (String) params.get("imageUrl");
        String imageMd5 = (String) params.get("imageMd5");
        Long imageSize = params.get("imageSize") != null ? Long.valueOf(params.get("imageSize").toString()) : null;
        String remark = (String) params.get("remark");

        Invoice invoice = invoiceService.uploadInvoice(
                invoiceCode, invoiceNumber, invoiceDate,
                amount, taxAmount, totalAmount,
                sellerName, sellerTaxNumber, buyerName, buyerTaxNumber,
                imageUrl, imageMd5, imageSize, remark
        );
        return Result.success("上传成功", invoice);
    }

    @PostMapping("/updateImage")
    public Result<Invoice> updateImage(@RequestBody Map<String, Object> params) {
        Long invoiceId = params.get("invoiceId") != null ? Long.valueOf(params.get("invoiceId").toString()) : null;
        String imageUrl = (String) params.get("imageUrl");
        String imageMd5 = (String) params.get("imageMd5");
        Long imageSize = params.get("imageSize") != null ? Long.valueOf(params.get("imageSize").toString()) : null;
        String changeReason = (String) params.get("changeReason");

        Invoice invoice = invoiceService.updateImage(invoiceId, imageUrl, imageMd5, imageSize, changeReason);
        return Result.success("影像更新成功", invoice);
    }

    @GetMapping("/{id}")
    public Result<Invoice> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return Result.success(invoice);
    }

    @GetMapping("/code/{invoiceCode}")
    public Result<Invoice> getInvoiceByCode(@PathVariable String invoiceCode) {
        Invoice invoice = invoiceService.getInvoiceByCode(invoiceCode);
        return Result.success(invoice);
    }

    @GetMapping("/list")
    public Result<List<Invoice>> getInvoiceList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String sellerName) {
        InvoiceStatus invoiceStatus = status != null ? InvoiceStatus.valueOf(status) : null;
        List<Invoice> list = invoiceService.getInvoiceList(invoiceStatus, department, sellerName);
        return Result.success(list);
    }

    @GetMapping("/{id}/versions")
    public Result<List<InvoiceImageVersion>> getImageVersions(@PathVariable Long id) {
        List<InvoiceImageVersion> versions = invoiceService.getImageVersionList(id);
        return Result.success(versions);
    }

    @GetMapping("/checkUnique")
    public Result<Boolean> checkUnique(@RequestParam String invoiceCode) {
        boolean unique = invoiceService.checkInvoiceUnique(invoiceCode);
        return Result.success(unique);
    }
}
