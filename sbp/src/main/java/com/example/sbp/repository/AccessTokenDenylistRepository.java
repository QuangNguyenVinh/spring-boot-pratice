package com.example.sbp.repository;

import com.example.sbp.model.AccessTokenDenylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenDenylistRepository extends JpaRepository<AccessTokenDenylist, UUID> {
}