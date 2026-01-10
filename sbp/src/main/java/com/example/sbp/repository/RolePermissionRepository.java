package com.example.sbp.repository;

import com.example.sbp.model.RolePermission;
import com.example.sbp.model.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}