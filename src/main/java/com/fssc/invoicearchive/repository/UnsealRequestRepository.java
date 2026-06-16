package com.fssc.invoicearchive.repository;

import com.fssc.invoicearchive.entity.UnsealRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnsealRequestRepository extends JpaRepository<UnsealRequest, Long> {

    List<UnsealRequest> findByBatchIdOrderByCreateTimeDesc(Long batchId);

    List<UnsealRequest> findByStatusOrderByCreateTimeDesc(String status);

    List<UnsealRequest> findByApplicantIdOrderByCreateTimeDesc(Long applicantId);

    boolean existsByBatchIdAndStatus(Long batchId, String status);
}
