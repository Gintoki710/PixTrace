package com.forgery.detector.repository;

import com.forgery.detector.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    Optional<AuditLogEntity> findTopByOrderByIdDesc();
    List<AuditLogEntity> findAllByOrderByIdAsc();
}

