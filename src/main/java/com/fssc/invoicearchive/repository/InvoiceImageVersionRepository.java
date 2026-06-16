package com.fssc.invoicearchive.repository;

import com.fssc.invoicearchive.entity.InvoiceImageVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceImageVersionRepository extends JpaRepository<InvoiceImageVersion, Long> {

    List<InvoiceImageVersion> findByInvoiceIdOrderByVersionNumberDesc(Long invoiceId);

    Optional<InvoiceImageVersion> findByInvoiceIdAndVersionNumber(Long invoiceId, Integer versionNumber);
}
