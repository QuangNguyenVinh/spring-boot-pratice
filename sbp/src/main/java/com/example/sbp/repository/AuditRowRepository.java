package com.example.sbp.repository;

import com.example.sbp.model.AuditRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditRowRepository extends JpaRepository<AuditRow, UUID> {
}