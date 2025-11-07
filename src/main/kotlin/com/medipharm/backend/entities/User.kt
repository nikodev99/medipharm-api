package com.medipharm.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.relational.core.mapping.Table
import jakarta.validation.constraints.*
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("email")
    @field:Email(message = "email must be valid")
    @field:NotBlank(message = "email must not be blank")
    val email: String,

    @Column("password")
    val password: String? = null,

    @Column("full_name")
    @field:NotBlank(message = "username must not be blank")
    @field:Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
    val fullName: String,

    @Column("phone_number")
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,9}$", message = "phone number must be valid")
    val phoneNumber: String? = null,

    @Column("role")
    val role: UserRole,

    @Column("auth_provider")
    val authProvider: AuthProvider = AuthProvider.LOCAL,

    @Column("provider_id")
    val providerId: String? = null,

    @Column("is_premium")
    val isPremium: Boolean = false,

    @Column("premium_expiry_date")
    val premiumExpiryDate: LocalDateTime? = null,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("is_email_verified")
    val emailVerified: Boolean = false,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_at")
    val updatedAt: LocalDateTime? = LocalDateTime.now()
)

enum class UserRole {
    USER, PHARMACY_ADMIN, SUPER_ADMIN
}

enum class AuthProvider {
    LOCAL, GOOGLE, FACEBOOK
}
