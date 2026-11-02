package com.forgery.detector.repository;

import com.forgery.detector.entity.ImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRecordRepository extends JpaRepository<ImageRecord, Long> {
    Optional<ImageRecord> findBySha256(String sha256);
    List<ImageRecord> findAllByOrderByAnalyzedAtDesc();
}

