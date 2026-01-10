package com.example.sbp.repository;

import com.example.sbp.model.UserRole;
import com.example.sbp.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}