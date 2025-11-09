package com.medipharm.backend.repository

import com.medipharm.backend.entities.Refresh
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

@Repository
interface RefreshRepository: R2dbcRepository<Refresh, Long> {
    fun findByRefreshTokenAndIsActiveTrue(refreshToken: String): Mono<Refresh>
    fun findByUserIdAndClientTypeAndIsActiveTrue(userId: Long, clientType: String?): Mono<Refresh>
    fun findByUserIdAndIsActiveTrue(userId: Long): Mono<Refresh>

    @Modifying
    @Query("UPDATE refresh_tokens SET last_used_at = now() WHERE refresh_token = :refreshToken")
    fun updateLastUsedAt(refreshToken: String): Mono<Int>

    @Modifying
    @Query("UPDATE refresh_tokens SET refresh_count = refresh_count + 1 WHERE refresh_token = :refreshToken")
    fun updateRefreshCount(refreshToken: String): Mono<Int>

    @Modifying
    @Query("UPDATE refresh_tokens SET revoked_at = now() WHERE refresh_token = :refreshToken")
    fun updateRevokedAt(refreshToken: String): Mono<Int>

    @Modifying
    @Query("UPDATE refresh_tokens SET is_active = :isActive WHERE refresh_token = :refreshToken")
    fun updateIsActive(refreshToken: String, isActive: Boolean): Mono<Int>

    fun deleteByUserIdAndClientType(userId: Long, clientType: String): Mono<Void>
    fun deleteByExpiryDateBefore(expiresAt: Instant): Mono<Void>

    @Query("SELECT rt.* FROM refresh_tokens rt WHERE rt.is_active = true AND rt.last_used_at < :cutoffDate")
    fun findStaleTokens(cutoffDate: Instant): Flux<Refresh>

    @Query("SELECT COUNT(rt.id) FROM refresh_tokens rt WHERE rt.is_active = true AND rt.user_id = :userId")
    fun countActiveTokensByUserId(userId: Long): Mono<Long>

    @Query("SELECT COUNT(rt.id) FROM refresh_tokens rt WHERE rt.is_active = true AND rt.client_type = :clientType")
    fun countActiveTokensByClientType(clientType: String): Mono<Long>
}