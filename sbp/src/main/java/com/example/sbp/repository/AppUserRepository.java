package com.example.sbp.repository;

import com.example.sbp.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "userRoles.role.rolePermissions",
            "userRoles.role.rolePermissions.permission"
    })
    @Query("""
                select distinct u
                from AppUser u
                left join fetch u.userRoles r
                left join fetch r.role.rolePermissions
                where u.id = :id
            """)
    Optional<AppUser> findByUserIdWithRolesAndPermissions(UUID id);

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "userRoles.role.rolePermissions",
            "userRoles.role.rolePermissions.permission"
    })
    @Query("""
                select distinct u
                from AppUser u
                left join fetch u.userRoles r
                left join fetch r.role.rolePermissions
                where u.username = :username
            """)
    Optional<AppUser> findByUsernameWithRolesAndPermissions(String username);
}