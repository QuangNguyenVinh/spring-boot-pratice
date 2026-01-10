package com.example.sbp.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    @Getter
    private final UUID userId;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final boolean locked;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID userId, String username, String passwordHash,
                         boolean enabled, boolean locked,
                         Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.locked = locked;
        this.authorities = authorities;
    }


    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !locked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }}
