package com.pms.auth.repository;

import com.pms.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);
    // Quick check: is this token blacklisted?

    // Delete all expired tokens from the table (cleanup — run nightly)
    // @Modifying = required for DELETE/UPDATE queries (not just SELECT)
    // @Transactional = wrap in a transaction
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
