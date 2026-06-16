package com.fssc.invoicearchive.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_code", unique = true, nullable = false, length = 50)
    private String invoiceCode;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "seller_name", nullable = false, length = 200)
    private String sellerName;

    @Column(name = "seller_tax_number", length = 50)
    private String sellerTaxNumber;

    @Column(name = "buyer_name", length = 200)
    private String buyerName;

    @Column(name = "buyer_tax_number", length = 50)
    private String buyerTaxNumber;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_version", nullable = false)
    private Integer imageVersion = 1;

    @Column(name = "image_md5", length = 100)
    private String imageMd5;

    @Column(name = "image_size")
    private Long imageSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.UPLOADED;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "uploader_name", length = 50)
    private String uploaderName;

    @Column(name = "upload_dept", length = 100)
    private String uploadDept;

    @Column(name = "reimburse_bill_id")
    private Long reimburseBillId;

    @Column(name = "reimburse_bill_no", length = 50)
    private String reimburseBillNo;

    @Column(name = "associate_time")
    private LocalDateTime associateTime;

    @Column(name = "associate_user_id")
    private Long associateUserId;

    @Column(name = "associate_user_name", length = 50)
    private String associateUserName;

    @Column(name = "archive_record_id")
    private Long archiveRecordId;

    @Column(name = "archive_box_no", length = 50)
    private String archiveBoxNo;

    @Column(name = "archive_position", length = 200)
    private String archivePosition;

    @Column(name = "archive_time")
    private LocalDateTime archiveTime;

    @Column(name = "archivist_id")
    private Long archivistId;

    @Column(name = "archivist_name", length = 50)
    private String archivistName;

    @Column(name = "archive_batch_id")
    private Long archiveBatchId;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "return_operator_id")
    private Long returnOperatorId;

    @Column(name = "return_operator_name", length = 50)
    private String returnOperatorName;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Version
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerTaxNumber() {
        return sellerTaxNumber;
    }

    public void setSellerTaxNumber(String sellerTaxNumber) {
        this.sellerTaxNumber = sellerTaxNumber;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerTaxNumber() {
        return buyerTaxNumber;
    }

    public void setBuyerTaxNumber(String buyerTaxNumber) {
        this.buyerTaxNumber = buyerTaxNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(Integer imageVersion) {
        this.imageVersion = imageVersion;
    }

    public String getImageMd5() {
        return imageMd5;
    }

    public void setImageMd5(String imageMd5) {
        this.imageMd5 = imageMd5;
    }

    public Long getImageSize() {
        return imageSize;
    }

    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploadDept() {
        return uploadDept;
    }

    public void setUploadDept(String uploadDept) {
        this.uploadDept = uploadDept;
    }

    public Long getReimburseBillId() {
        return reimburseBillId;
    }

    public void setReimburseBillId(Long reimburseBillId) {
        this.reimburseBillId = reimburseBillId;
    }

    public String getReimburseBillNo() {
        return reimburseBillNo;
    }

    public void setReimburseBillNo(String reimburseBillNo) {
        this.reimburseBillNo = reimburseBillNo;
    }

    public LocalDateTime getAssociateTime() {
        return associateTime;
    }

    public void setAssociateTime(LocalDateTime associateTime) {
        this.associateTime = associateTime;
    }

    public Long getAssociateUserId() {
        return associateUserId;
    }

    public void setAssociateUserId(Long associateUserId) {
        this.associateUserId = associateUserId;
    }

    public String getAssociateUserName() {
        return associateUserName;
    }

    public void setAssociateUserName(String associateUserName) {
        this.associateUserName = associateUserName;
    }

    public Long getArchiveRecordId() {
        return archiveRecordId;
    }

    public void setArchiveRecordId(Long archiveRecordId) {
        this.archiveRecordId = archiveRecordId;
    }

    public String getArchiveBoxNo() {
        return archiveBoxNo;
    }

    public void setArchiveBoxNo(String archiveBoxNo) {
        this.archiveBoxNo = archiveBoxNo;
    }

    public String getArchivePosition() {
        return archivePosition;
    }

    public void setArchivePosition(String archivePosition) {
        this.archivePosition = archivePosition;
    }

    public LocalDateTime getArchiveTime() {
        return archiveTime;
    }

    public void setArchiveTime(LocalDateTime archiveTime) {
        this.archiveTime = archiveTime;
    }

    public Long getArchivistId() {
        return archivistId;
    }

    public void setArchivistId(Long archivistId) {
        this.archivistId = archivistId;
    }

    public String getArchivistName() {
        return archivistName;
    }

    public void setArchivistName(String archivistName) {
        this.archivistName = archivistName;
    }

    public Long getArchiveBatchId() {
        return archiveBatchId;
    }

    public void setArchiveBatchId(Long archiveBatchId) {
        this.archiveBatchId = archiveBatchId;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }

    public Long getReturnOperatorId() {
        return returnOperatorId;
    }

    public void setReturnOperatorId(Long returnOperatorId) {
        this.returnOperatorId = returnOperatorId;
    }

    public String getReturnOperatorName() {
        return returnOperatorName;
    }

    public void setReturnOperatorName(String returnOperatorName) {
        this.returnOperatorName = returnOperatorName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
