package com.example.sbp.security.filter;

import com.example.sbp.security.component.DbAuditContextSetter;
import com.example.sbp.security.model.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Run after JWT Auth and before business logic
 */
public class DbAuditFilter extends OncePerRequestFilter {

    private final DbAuditContextSetter setter;

    public DbAuditFilter(DbAuditContextSetter setter) {
        this.setter = setter;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        UUID requestId = UUID.randomUUID();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = null;
        String username = null;

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal p) {
            userId = p.getUserId();
            username = p.getUsername();
        }

        String ip = clientIp(request);
        String ua = request.getHeader("User-Agent");

        // One SQL statement to set session vars
        setter.set(userId, username, requestId, ip, ua);

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
