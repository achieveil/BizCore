package com.achieveil.bizcore.service;

import com.achieveil.bizcore.entity.UserSession;
import com.achieveil.bizcore.repository.UserSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSession createSession(Long userId,
                                     String deviceId,
                                     String refreshToken,
                                     LocalDateTime expiresAt,
                                     String userAgent,
                                     String ipAddress) {
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setDeviceId(deviceId);
        session.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
        session.setExpiresAt(expiresAt);
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        session.setLastUsedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public Optional<UserSession> findActiveSession(String sessionId) {
        return sessionRepository.findByIdAndRevokedFalse(sessionId)
                .filter(session -> session.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Transactional
    public void revokeSession(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setRevoked(true);
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void revokeAllSessions(Long userId) {
        sessionRepository.findByUserIdAndRevokedFalse(userId).forEach(session -> {
            session.setRevoked(true);
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void rotateRefreshToken(UserSession session, String newRefreshToken, LocalDateTime newExpiry) {
        session.setRefreshTokenHash(passwordEncoder.encode(newRefreshToken));
        session.setExpiresAt(newExpiry);
        session.setLastUsedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    public boolean matches(UserSession session, String rawToken) {
        return passwordEncoder.matches(rawToken, session.getRefreshTokenHash());
    }

    @Transactional
    public void touchSession(UserSession session) {
        session.setLastUsedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }
}
