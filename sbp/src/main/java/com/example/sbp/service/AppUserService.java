package com.example.sbp.service;

import com.example.sbp.mapper.UserPrincipalMapper;
import com.example.sbp.repository.AppUserRepository;
import com.example.sbp.security.model.UserPrincipal;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return userPrincipalMapper.toPrincipal(user);
    }

    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(UUID userId) {
        var user = appUserRepository.findByUserIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return userPrincipalMapper.toPrincipal(user);
    }
}
