package com.fssc.invoicearchive.repository;

import com.fssc.invoicearchive.entity.ReimburseBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReimburseBillRepository extends JpaRepository<ReimburseBill, Long> {

    Optional<ReimburseBill> findByBillNo(String billNo);

    boolean existsByBillNo(String billNo);
}
