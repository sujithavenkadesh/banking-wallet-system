package com.banking.wallet.repository;

import com.banking.wallet.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy);
}