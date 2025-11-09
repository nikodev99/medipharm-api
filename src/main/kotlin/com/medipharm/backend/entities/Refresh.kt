package com.medipharm.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("refresh_tokens")
data class Refresh(
    @Id
    val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("refresh_token")
    val refreshToken: String,

    @Column("user_agent")
    val userAgent: String? = null,

    @Column("client_ip")
    val  clientIp: String? = null,

    @Column("expiry_date")
    val  expiryDate: Instant,

    @Column("created_at")
    val  createdAt: Instant? = Instant.now(),

    @Column("last_used_at")
    val lastUsedAt: Instant? = Instant.now(),

    @Column("revoked_at")
    val revokedAt: Instant? = null,

    @Column("is_active")
    val isActive: Boolean? = true,

    @Column("device_fingerprint")
    val deviceFingerprint: String? = null, // For more sophisticated device tracking

    @Column("refresh_count")
    val refreshCount: Int? = 0,

    @Column("client_type")
    val clientType: String? = null,
)