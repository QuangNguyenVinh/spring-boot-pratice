package com.example.sbp.service;

import com.example.sbp.model.AppUser;
import com.example.sbp.model.AuthSession;
import com.example.sbp.repository.AuthSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.UUID;

@Service
public class SessionService {

    private final AuthSessionRepository authSessionRepository;

    public SessionService(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    @Transactional
    public AuthSession create(UUID userId, String ip, String userAgent, String deviceId) throws UnknownHostException {
        AppUser userRef = new AppUser();
        userRef.setId(userId);

        AuthSession s = new AuthSession();
        s.setUser(userRef);
        s.setDeviceId(deviceId);
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
        s.setLastSeenAt(Instant.now());
        s.setIp(InetAddress.getByName(ip));
        s.setUserAgent(userAgent);
        s.setRevoked(false);

        return authSessionRepository.save(s);
    }
}

