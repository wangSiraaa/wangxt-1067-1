package com.fssc.invoicearchive.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "archive_batch")
public class ArchiveBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no", unique = true, nullable = false, length = 50)
    private String batchNo;

    @Column(name = "batch_name", length = 200)
    private String batchName;

    @Column(name = "invoice_count")
    private Integer invoiceCount = 0;

    @Column(name = "archivist_id")
    private Long archivistId;

    @Column(name = "archivist_name", length = 50)
    private String archivistName;

    @Column(name = "sealed")
    private Boolean sealed = false;

    @Column(name = "seal_time")
    private LocalDateTime sealTime;

    @Column(name = "seal_operator_id")
    private Long sealOperatorId;

    @Column(name = "seal_operator_name", length = 50)
    private String sealOperatorName;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public Integer getInvoiceCount() {
        return invoiceCount;
    }

    public void setInvoiceCount(Integer invoiceCount) {
        this.invoiceCount = invoiceCount;
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

    public Boolean getSealed() {
        return sealed;
    }

    public void setSealed(Boolean sealed) {
        this.sealed = sealed;
    }

    public LocalDateTime getSealTime() {
        return sealTime;
    }

    public void setSealTime(LocalDateTime sealTime) {
        this.sealTime = sealTime;
    }

    public Long getSealOperatorId() {
        return sealOperatorId;
    }

    public void setSealOperatorId(Long sealOperatorId) {
        this.sealOperatorId = sealOperatorId;
    }

    public String getSealOperatorName() {
        return sealOperatorName;
    }

    public void setSealOperatorName(String sealOperatorName) {
        this.sealOperatorName = sealOperatorName;
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
