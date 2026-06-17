package com.fssc.invoicearchive.service;

import com.fssc.invoicearchive.common.BusinessException;
import com.fssc.invoicearchive.context.UserContext;
import com.fssc.invoicearchive.entity.*;
import com.fssc.invoicearchive.repository.InvoiceImageVersionRepository;
import com.fssc.invoicearchive.repository.InvoiceRepository;
import com.fssc.invoicearchive.repository.ArchiveBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceImageVersionRepository imageVersionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ArchiveBatchRepository archiveBatchRepository;

    @Transactional
    public Invoice uploadInvoice(String invoiceCode, String invoiceNumber, LocalDate invoiceDate,
                               BigDecimal amount, BigDecimal taxAmount, BigDecimal totalAmount,
                               String sellerName, String sellerTaxNumber, String buyerName, String buyerTaxNumber,
                               String imageUrl, String imageMd5, Long imageSize, String remark) {

        userService.checkRole(RoleType.ACCOUNTANT);

        if (invoiceCode == null || invoiceCode.trim().isEmpty()) {
            throw new BusinessException("票据代码不能为空");
        }
        if (amount == null) {
            throw new BusinessException("票据金额不能为空");
        }
        if (sellerName == null || sellerName.trim().isEmpty()) {
            throw new BusinessException("开票方不能为空");
        }
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new BusinessException("票据影像不能为空");
        }

        if (invoiceRepository.existsByInvoiceCode(invoiceCode)) {
            throw new BusinessException("票据代码已存在，不能重复上传");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(invoiceCode);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setAmount(amount);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setSellerName(sellerName);
        invoice.setSellerTaxNumber(sellerTaxNumber);
        invoice.setBuyerName(buyerName);
        invoice.setBuyerTaxNumber(buyerTaxNumber);
        invoice.setImageUrl(imageUrl);
        invoice.setImageVersion(1);
        invoice.setImageMd5(imageMd5);
        invoice.setImageSize(imageSize);
        invoice.setStatus(InvoiceStatus.UPLOADED);
        invoice.setUploaderId(UserContext.getCurrentUserId());
        invoice.setUploaderName(UserContext.getCurrentUserName());
        invoice.setUploadDept(UserContext.getCurrentUserDept());
        invoice.setRemark(remark);

        invoice = invoiceRepository.save(invoice);

        saveImageVersion(invoice, 1, "首次上传");

        auditLogService.logOperation(
                invoice.getId(),
                invoice.getInvoiceCode(),
                "UPLOAD",
                "上传票据影像",
                null,
                InvoiceStatus.UPLOADED,
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.ACCOUNTANT
        );

        return invoice;
    }

    @Transactional
    public Invoice updateImage(Long invoiceId, String imageUrl, String imageMd5, Long imageSize, String changeReason) {

        userService.checkRole(RoleType.ACCOUNTANT);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("票据不存在"));

        if (invoice.getStatus() == InvoiceStatus.SEALED 
                || invoice.getStatus() == InvoiceStatus.AUDIT_SPOTCHECK
                || invoice.getStatus() == InvoiceStatus.ARCHIVED) {
            throw new BusinessException("已归档票据不能更换影像，请先申请退回补扫");
        }

        if (changeReason == null || changeReason.trim().isEmpty()) {
            throw new BusinessException("补扫影像必须填写变更原因");
        }

        int newVersion = invoice.getImageVersion() + 1;
        invoice.setImageUrl(imageUrl);
        invoice.setImageVersion(newVersion);
        invoice.setImageMd5(imageMd5);
        invoice.setImageSize(imageSize);

        invoice = invoiceRepository.save(invoice);

        saveImageVersion(invoice, newVersion, changeReason);

        auditLogService.logOperation(
                invoice.getId(),
                invoice.getInvoiceCode(),
                "IMAGE_UPDATE",
                "更新票据影像，版本号: " + newVersion + "，原因: " + changeReason,
                invoice.getStatus(),
                invoice.getStatus(),
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.ACCOUNTANT
        );

        return invoice;
    }

    private void saveImageVersion(Invoice invoice, Integer version, String changeReason) {
        InvoiceImageVersion versionEntity = new InvoiceImageVersion();
        versionEntity.setInvoiceId(invoice.getId());
        versionEntity.setInvoiceCode(invoice.getInvoiceCode());
        versionEntity.setVersionNumber(version);
        versionEntity.setImageUrl(invoice.getImageUrl());
        versionEntity.setImageMd5(invoice.getImageMd5());
        versionEntity.setImageSize(invoice.getImageSize());
        versionEntity.setUploaderId(UserContext.getCurrentUserId());
        versionEntity.setUploaderName(UserContext.getCurrentUserName());
        versionEntity.setChangeReason(changeReason);
        imageVersionRepository.save(versionEntity);
    }

    public Invoice getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("票据不存在"));

        if (!userService.canViewDepartment(invoice.getUploadDept())) {
            throw new BusinessException("无权查看其他部门的票据");
        }

        return invoice;
    }

    public Invoice getInvoiceByCode(String invoiceCode) {
        Invoice invoice = invoiceRepository.findByInvoiceCode(invoiceCode)
                .orElseThrow(() -> new BusinessException("票据不存在"));

        if (!userService.canViewDepartment(invoice.getUploadDept())) {
            throw new BusinessException("无权查看其他部门的票据");
        }

        return invoice;
    }

    public List<Invoice> getInvoiceList(InvoiceStatus status, String department, String sellerName) {
        Specification<Invoice> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (sellerName != null && !sellerName.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("sellerName"), "%" + sellerName + "%"));
        }

        if (!UserContext.hasRole(RoleType.AUDITOR) && !UserContext.hasRole(RoleType.ARCHIVIST)) {
            String userDept = UserContext.getCurrentUserDept();
            if (userDept != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("uploadDept"), userDept));
            }
        }

        List<Invoice> list = invoiceRepository.findAll(spec);
        return list;
    }

    public List<InvoiceImageVersion> getImageVersionList(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        return imageVersionRepository.findByInvoiceIdOrderByVersionNumberDesc(invoiceId);
    }

    public boolean checkInvoiceUnique(String invoiceCode) {
        return !invoiceRepository.existsByInvoiceCode(invoiceCode);
    }

    @Transactional
    public Invoice redBackInvoice(Long originalInvoiceId, String redReason) {
        userService.checkRole(RoleType.ACCOUNTANT);

        Invoice original = invoiceRepository.findById(originalInvoiceId)
                .orElseThrow(() -> new BusinessException("原票据不存在"));

        if (original.getRedInvoiceFlag()) {
            throw new BusinessException("红冲票据不能再做红冲");
        }

        if (original.getRedInvoiceId() != null) {
            throw new BusinessException("该票据已有红冲票据");
        }

        Invoice redInvoice = new Invoice();
        redInvoice.setInvoiceCode(original.getInvoiceCode() + "-R");
        redInvoice.setInvoiceNumber(original.getInvoiceNumber());
        redInvoice.setInvoiceDate(original.getInvoiceDate());
        redInvoice.setAmount(original.getAmount().negate());
        redInvoice.setTaxAmount(original.getTaxAmount() != null ? original.getTaxAmount().negate() : null);
        redInvoice.setTotalAmount(original.getTotalAmount() != null ? original.getTotalAmount().negate() : null);
        redInvoice.setSellerName(original.getSellerName());
        redInvoice.setSellerTaxNumber(original.getSellerTaxNumber());
        redInvoice.setBuyerName(original.getBuyerName());
        redInvoice.setBuyerTaxNumber(original.getBuyerTaxNumber());
        redInvoice.setImageUrl(original.getImageUrl());
        redInvoice.setImageVersion(1);
        redInvoice.setImageMd5(original.getImageMd5());
        redInvoice.setImageSize(original.getImageSize());
        redInvoice.setStatus(InvoiceStatus.UPLOADED);
        redInvoice.setUploaderId(UserContext.getCurrentUserId());
        redInvoice.setUploaderName(UserContext.getCurrentUserName());
        redInvoice.setUploadDept(UserContext.getCurrentUserDept());
        redInvoice.setRedInvoiceFlag(true);
        redInvoice.setOriginalInvoiceId(originalInvoiceId);
        redInvoice.setRedReason(redReason);
        redInvoice.setArchiveBoxNo(original.getArchiveBoxNo());
        redInvoice.setArchivePosition(original.getArchivePosition());
        redInvoice.setArchiveBatchId(original.getArchiveBatchId());

        redInvoice = invoiceRepository.save(redInvoice);

        saveImageVersion(redInvoice, 1, "红冲票据，关联原票: " + original.getInvoiceCode());

        original.setRedInvoiceId(redInvoice.getId());
        invoiceRepository.save(original);

        auditLogService.logOperation(
                original.getId(),
                original.getInvoiceCode(),
                "RED_BACK",
                "红冲票据，红冲票号: " + redInvoice.getInvoiceCode() + "，原因: " + redReason,
                original.getStatus(),
                original.getStatus(),
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.ACCOUNTANT
        );

        auditLogService.logOperation(
                redInvoice.getId(),
                redInvoice.getInvoiceCode(),
                "RED_BACK_CREATE",
                "红冲票据创建，原票: " + original.getInvoiceCode() + "，原因: " + redReason,
                null,
                InvoiceStatus.UPLOADED,
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.ACCOUNTANT
        );

        return redInvoice;
    }
}
