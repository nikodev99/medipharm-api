package com.medipharm.backend.services

import com.medipharm.backend.entities.Refresh
import com.medipharm.backend.repository.RefreshRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RefreshService(
    private val refreshRepository: RefreshRepository
) {

    private val refreshTtlDays: Long = 30 // refresh token lifetime

    /**
     * Save or replace a refresh token for the same user + client type/browser.
     * Only one active token per device/browser.
     */
    fun saveRefreshToken(refresh: Refresh): Mono<Refresh> {
        return refreshRepository
            .findByUserIdAndClientTypeAndIsActiveTrue(refresh.userId, refresh.clientType)
            .flatMap { existing ->
                // deactivate an existing token before saving the new one
                refreshRepository.updateIsActive(existing.refreshToken, false)
                    .then(refreshRepository.updateRevokedAt(existing.refreshToken))
                    .then(refreshRepository.save(refresh))
            }
            .switchIfEmpty(refreshRepository.save(refresh))
    }

    /**
     * Fetch and validate a refresh token by its string.
     * If expired, deactivate it and emit ResponseStatusException(UNAUTHORIZED).
     */
    fun validateRefreshToken(refreshToken: String): Mono<Refresh> {
        val now = Instant.now()
        return refreshRepository.findByRefreshTokenAndIsActiveTrue(refreshToken)
            .switchIfEmpty(
                Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or inactive refresh token"))
            )
            .flatMap { token ->
                if (token.expiryDate.isBefore(now)) {
                    // mark inactive when expired
                    refreshRepository.updateIsActive(refreshToken, false)
                        .then(refreshRepository.updateRevokedAt(refreshToken))
                        .then(
                            Mono.error(
                                ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired")
                            )
                        )
                } else {
                    Mono.just(token)
                }
            }
    }

    /**
     * When /auth/refresh is called.
     *  - Validate token
     *  - Update lastUsedAt, refreshCount
     *  - Extend expiryDate 30 days from now
     */
    fun refreshTokenUsage(refreshToken: String): Mono<Refresh> {
        return validateRefreshToken(refreshToken)
            .flatMap { token ->
                Mono.zip(
                    refreshRepository.updateLastUsedAt(refreshToken),
                    refreshRepository.updateRefreshCount(refreshToken)
                ).then(
                    refreshRepository.save(
                        token.copy(
                            lastUsedAt = Instant.now(),
                        )
                    )
                )
            }
    }

    /**
     * Revoke a single refresh token (logout on one device).
     */
    fun revokeToken(refreshToken: String): Mono<Void> {
        return refreshRepository.updateIsActive(refreshToken, false)
            .then(refreshRepository.updateRevokedAt(refreshToken))
            .then()
    }

    /**
     * Logout all active sessions for a user.
     * Revoke every active token for this user.
     */
    fun revokeAllTokensForUser(userId: Long): Mono<Void> {
        return findAllActiveTokensByUser(userId)
            .flatMap { token ->
                refreshRepository.updateIsActive(token.refreshToken, false)
                    .then(refreshRepository.updateRevokedAt(token.refreshToken))
            }
            .then()
    }

    /**
     * Find all active refresh tokens of a user (for session management UI or logout-all).
     */
    fun findAllActiveTokensByUser(userId: Long): Flux<Refresh> {
        return refreshRepository.findByUserIdAndIsActiveTrue(userId)
            .flux()
    }

    /**
     * Utility: create a ready-to-save Refresh instance.
     */
    fun buildRefreshTokenEntity(
        userId: Long,
        refreshToken: String,
        userAgent: String,
        clientIp: String,
        clientType: String,
        deviceFingerprint: String?
    ): Refresh {
        val now = Instant.now()
        return Refresh(
            id = null,
            userId = userId,
            refreshToken = refreshToken,
            userAgent = userAgent,
            clientIp = clientIp,
            expiryDate = now.plus(refreshTtlDays, ChronoUnit.DAYS),
            createdAt = now,
            lastUsedAt = now,
            revokedAt = Instant.EPOCH,
            isActive = true,
            deviceFingerprint = deviceFingerprint ?: "",
            refreshCount = 0,
            clientType = clientType
        )
    }

    /**
     * Cleanup job helper: delete expired tokens in batch.
     */
    fun cleanupExpiredTokens(): Mono<Void> {
        val now = Instant.now()
        return refreshRepository.deleteByExpiryDateBefore(now)
    }
}
