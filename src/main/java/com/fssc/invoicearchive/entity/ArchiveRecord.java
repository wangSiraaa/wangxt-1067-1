package com.fssc.invoicearchive.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "archive_record")
public class ArchiveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archive_no", unique = true, nullable = false, length = 50)
    private String archiveNo;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "invoice_code", nullable = false, length = 50)
    private String invoiceCode;

    @Column(name = "archive_box_no", length = 50)
    private String archiveBoxNo;

    @Column(name = "archive_position", length = 200)
    private String archivePosition;

    @Column(name = "archivist_id", nullable = false)
    private Long archivistId;

    @Column(name = "archivist_name", length = 50)
    private String archivistName;

    @Column(name = "archive_batch_id")
    private Long archiveBatchId;

    @Column(name = "archive_time")
    private LocalDateTime archiveTime;

    @Column(name = "status", length = 20)
    private String status = "NORMAL";

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArchiveNo() {
        return archiveNo;
    }

    public void setArchiveNo(String archiveNo) {
        this.archiveNo = archiveNo;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
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

    public LocalDateTime getArchiveTime() {
        return archiveTime;
    }

    public void setArchiveTime(LocalDateTime archiveTime) {
        this.archiveTime = archiveTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (archiveTime == null) {
            archiveTime = LocalDateTime.now();
        }
    }
}
