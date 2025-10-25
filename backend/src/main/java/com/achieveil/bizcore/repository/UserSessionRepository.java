package com.achieveil.bizcore.repository;

import com.achieveil.bizcore.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findByIdAndRevokedFalse(String id);

    List<UserSession> findByUserIdAndRevokedFalse(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime timestamp);
}
