package com.example.sbp.security.filter;

import com.example.sbp.security.component.JwtService;
import com.example.sbp.security.model.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException, ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var jwt = jwtService.parseAndValidate(token);

                UUID userId = UUID.fromString(jwt.getClaimAsString("uid"));
                String username = jwt.getSubject();

                List<String> authList = jwt.getClaimAsStringList("auth");

                var authorities = authList == null ? List.<SimpleGrantedAuthority>of()
                        : authList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());

                // No password needed for JWT-authenticated requests
                var principal = new UserPrincipal(userId, username, "", true, false, authorities);

                var authentication = new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception ex) {
                log.error("Error when trying to extract jwt token due to {}",ex.toString());
            }
        }

        filterChain.doFilter(request, response);
    }
}
