package com.fssc.invoicearchive.service;

import com.fssc.invoicearchive.common.BusinessException;
import com.fssc.invoicearchive.context.UserContext;
import com.fssc.invoicearchive.entity.*;
import com.fssc.invoicearchive.repository.InvoiceRepository;
import com.fssc.invoicearchive.repository.ReimburseBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReimburseService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ReimburseBillRepository reimburseBillRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public Invoice associateInvoice(Long invoiceId, Long reimburseBillId) {

        userService.checkRole(RoleType.DEPT_HANDLER);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("票据不存在"));

        if (invoice.getStatus() != InvoiceStatus.UPLOADED && invoice.getStatus() != InvoiceStatus.RETURNED) {
            throw new BusinessException("当前票据状态不允许关联报销单，状态: " + invoice.getStatus());
        }

        if (invoice.getReimburseBillId() != null) {
            throw new BusinessException("该票据已关联报销单，不能重复关联");
        }

        ReimburseBill bill = reimburseBillRepository.findById(reimburseBillId)
                .orElseThrow(() -> new BusinessException("报销单不存在"));

        if (!"APPROVED".equals(bill.getBillStatus()) && !"SUBMITTED".equals(bill.getBillStatus())) {
            throw new BusinessException("报销单状态不正确，当前状态: " + bill.getBillStatus());
        }

        if (!userService.canViewDepartment(bill.getDepartment())) {
            throw new BusinessException("无权关联其他部门的报销单");
        }

        List<Invoice> associatedInvoices = invoiceRepository.findByReimburseBillId(reimburseBillId);
        BigDecimal totalInvoiceAmount = invoice.getAmount();
        for (Invoice inv : associatedInvoices) {
            totalInvoiceAmount = totalInvoiceAmount.add(inv.getAmount());
        }

        if (totalInvoiceAmount.compareTo(bill.getAmount()) > 0) {
            throw new BusinessException("票据总金额超过报销单金额，票据金额: " + totalInvoiceAmount
                    + "，报销单金额: " + bill.getAmount());
        }

        InvoiceStatus beforeStatus = invoice.getStatus();

        invoice.setReimburseBillId(reimburseBillId);
        invoice.setReimburseBillNo(bill.getBillNo());
        invoice.setStatus(InvoiceStatus.ASSOCIATED);
        invoice.setAssociateTime(LocalDateTime.now());
        invoice.setAssociateUserId(UserContext.getCurrentUserId());
        invoice.setAssociateUserName(UserContext.getCurrentUserName());

        invoice = invoiceRepository.save(invoice);

        bill.setInvoiceAmount(totalInvoiceAmount);
        reimburseBillRepository.save(bill);

        auditLogService.logOperation(
                invoice.getId(),
                invoice.getInvoiceCode(),
                "ASSOCIATE",
                "关联报销单: " + bill.getBillNo(),
                beforeStatus,
                InvoiceStatus.ASSOCIATED,
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.DEPT_HANDLER
        );

        return invoice;
    }

    @Transactional
    public Invoice disassociateInvoice(Long invoiceId, String reason) {

        userService.checkRole(RoleType.DEPT_HANDLER);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("票据不存在"));

        if (invoice.getStatus() == InvoiceStatus.ARCHIVED || invoice.getStatus() == InvoiceStatus.SEALED) {
            throw new BusinessException("已归档票据不能取消关联");
        }

        if (invoice.getReimburseBillId() == null) {
            throw new BusinessException("该票据未关联报销单");
        }

        Long billId = invoice.getReimburseBillId();
        String billNo = invoice.getReimburseBillNo();
        InvoiceStatus beforeStatus = invoice.getStatus();

        invoice.setReimburseBillId(null);
        invoice.setReimburseBillNo(null);
        invoice.setStatus(InvoiceStatus.UPLOADED);
        invoice.setAssociateTime(null);
        invoice.setAssociateUserId(null);
        invoice.setAssociateUserName(null);

        invoice = invoiceRepository.save(invoice);

        ReimburseBill bill = reimburseBillRepository.findById(billId).orElse(null);
        if (bill != null) {
            List<Invoice> associatedInvoices = invoiceRepository.findByReimburseBillId(billId);
            BigDecimal totalInvoiceAmount = BigDecimal.ZERO;
            for (Invoice inv : associatedInvoices) {
                totalInvoiceAmount = totalInvoiceAmount.add(inv.getAmount());
            }
            bill.setInvoiceAmount(totalInvoiceAmount);
            reimburseBillRepository.save(bill);
        }

        auditLogService.logOperation(
                invoice.getId(),
                invoice.getInvoiceCode(),
                "DISASSOCIATE",
                "取消关联报销单: " + billNo + "，原因: " + reason,
                beforeStatus,
                InvoiceStatus.UPLOADED,
                UserContext.getCurrentUserId(),
                UserContext.getCurrentUserName(),
                RoleType.DEPT_HANDLER
        );

        return invoice;
    }

    public ReimburseBill createReimburseBill(ReimburseBill bill) {
        userService.checkRole(RoleType.DEPT_HANDLER);

        if (bill.getBillNo() == null || bill.getBillNo().trim().isEmpty()) {
            throw new BusinessException("报销单号不能为空");
        }

        if (reimburseBillRepository.existsByBillNo(bill.getBillNo())) {
            throw new BusinessException("报销单号已存在");
        }

        if (bill.getAmount() == null) {
            throw new BusinessException("报销单金额不能为空");
        }

        bill.setApplicantId(UserContext.getCurrentUserId());
        bill.setApplicantName(UserContext.getCurrentUserName());
        bill.setDepartment(UserContext.getCurrentUserDept());
        bill.setBillStatus("SUBMITTED");

        return reimburseBillRepository.save(bill);
    }

    public ReimburseBill getReimburseBill(Long id) {
        return reimburseBillRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报销单不存在"));
    }

    public ReimburseBill getReimburseBillByNo(String billNo) {
        return reimburseBillRepository.findByBillNo(billNo)
                .orElseThrow(() -> new BusinessException("报销单不存在"));
    }

    public List<ReimburseBill> getAllReimburseBills() {
        return reimburseBillRepository.findAll();
    }

    public void initDefaultReimburseBills() {
        if (reimburseBillRepository.count() > 0) {
            return;
        }

        ReimburseBill bill1 = new ReimburseBill();
        bill1.setBillNo("BX202401001");
        bill1.setBillType("差旅费");
        bill1.setAmount(new BigDecimal("5000.00"));
        bill1.setDepartment("销售部");
        bill1.setBillStatus("APPROVED");
        bill1.setDescription("出差北京差旅费");
        reimburseBillRepository.save(bill1);

        ReimburseBill bill2 = new ReimburseBill();
        bill2.setBillNo("BX202401002");
        bill2.setBillType("办公费");
        bill2.setAmount(new BigDecimal("2000.00"));
        bill2.setDepartment("销售部");
        bill2.setBillStatus("APPROVED");
        bill2.setDescription("办公用品采购");
        reimburseBillRepository.save(bill2);

        ReimburseBill bill3 = new ReimburseBill();
        bill3.setBillNo("BX202401003");
        bill3.setBillType("业务招待费");
        bill3.setAmount(new BigDecimal("3000.00"));
        bill3.setDepartment("财务部");
        bill3.setBillStatus("SUBMITTED");
        bill3.setDescription("客户招待费用");
        reimburseBillRepository.save(bill3);
    }
}
