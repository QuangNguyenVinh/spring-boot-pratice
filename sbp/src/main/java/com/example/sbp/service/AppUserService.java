package com.example.sbp.service;

import com.example.sbp.mapper.UserPrincipalMapper;
import com.example.sbp.model.AppUser;
import com.example.sbp.repository.AppUserRepository;
import com.example.sbp.security.model.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final UserPrincipalMapper userPrincipalMapper;

    public AppUserService(AppUserRepository appUserRepository, UserPrincipalMapper userPrincipalMapper) {
        this.appUserRepository = appUserRepository;
        this.userPrincipalMapper = userPrincipalMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = appUserRepository.findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        var authorities = user.getUserRoles().stream()
                .flatMap(role -> role.getRole().getRolePermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission().getName()))
                .collect(Collectors.toUnmodifiableSet());

        return new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.getEnabled(), user.getLocked(), authorities);
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(UUID userId) {
        AppUser u = appUserRepository.findByUserIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return userPrincipalMapper.toPrincipal(u);
    }
}
