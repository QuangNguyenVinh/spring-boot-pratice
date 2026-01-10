package com.example.sbp.mapper;

import com.example.sbp.model.AppUser;
import com.example.sbp.security.model.UserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserPrincipalMapper {
    public UserPrincipal toPrincipal(AppUser u) {
        Set<GrantedAuthority> auths = u.getUserRoles().stream()
                .flatMap(r -> r.getRole().getRolePermissions().stream())
                .map(p -> new SimpleGrantedAuthority(p.getPermission().getName()))
                .collect(Collectors.toUnmodifiableSet());

        return new UserPrincipal(
                u.getId(),
                u.getUsername(),
                u.getPasswordHash(),
                u.getEnabled(),
                u.getLocked(),
                auths
        );
    }
}
