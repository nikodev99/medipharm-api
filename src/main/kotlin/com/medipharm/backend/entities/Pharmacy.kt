package com.medipharm.backend.entities

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("pharmacies")
data class Pharmacy(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("name")
    @field:NotBlank(message = "name must not be blank")
    @field:Size(min = 3, max = 200, message = "name must be between 3 and 200 characters")
    val name: String,

    @Column("address")
    @field:NotBlank(message = "address must not be blank")
    val address: String,

    @Column("city")
    val city: String,

    @Column("country")
    val country: String,

    @Column("phone_number")
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,9}$", message = "phone number must be valid")
    val phoneNumber: String?,

    @Column("alternate_phone_number")
    val alternatePhoneNumber: String? = null,

    @Column("website")
    val website: String? = null,

    @Column("email")
    val email: String? = null,

    @Column("latitude")
    val latitude: Double,

    @Column("longitude")
    val longitude: Double,

    @Column("description")
    val description: String? = null,

    @Column("logo_url")
    val logoUrl: String? = null,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("is_verified")
    val isVerified: Boolean = false,

    @Column("licence_number")
    val licenceNumber: String? = null,

    @Column("admin_id")
    val adminId: Long? = null,

    @Column("average_rating")
    val averageRating: Double? = 0.0,

    @Column("total_reviews")
    val totalReviews: Int = 0,

    @CreatedDate
    @Column("create_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column("update_at")
    val updatedAt: LocalDateTime? = LocalDateTime.now()
)

data class PharmacyDto(
    val id: Long? = null,
    val name: String,
    val address: String,
    val city: String,
    val country: String,
    val phoneNumber: String?,
    val alternatePhoneNumber: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val logoUrl: String? = null,
    val averageRating: Double? = 0.0,
    val totalReviews: Int = 0,
    val website: String? = null,
    val email: String? = null,
)

fun Pharmacy.toDto(): PharmacyDto = PharmacyDto(
    id = this.id,
    name = this.name,
    address = this.address,
    city = this.city,
    country = this.country,
    phoneNumber = this.phoneNumber,
    alternatePhoneNumber = this.alternatePhoneNumber,
    latitude = this.latitude,
    longitude = this.longitude,
    description = this.description,
    logoUrl = this.logoUrl,
    averageRating = this.averageRating,
    totalReviews = this.totalReviews,
    website = this.website,
    email = this.email,
)

fun Pharmacy.toDetailDto(admin: PharmacyAdminDto?, inventoryCount: Int) = PharmacyDetailDto(
    id = id!!,
    name = name,
    address = address,
    city = city,
    phoneNumber = phoneNumber ?: "",
    alternatePhoneNumber = alternatePhoneNumber,
    email = email,
    latitude = latitude,
    longitude = longitude,
    description = description,
    logoUrl = logoUrl,
    isActive = isActive,
    isVerified = isVerified,
    licenseNumber = this.licenceNumber,
    averageRating = averageRating ?: 0.0,
    totalReviews = totalReviews,
    admin = admin,
    inventoryCount = inventoryCount,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

data class PharmacyAvailability(
    val pharmacy: PharmacyDto,
    val price: Double,
    val quantity: Int,
    val distance: Double?
)

data class UpdatePharmacyRequest(
    val name: String?,
    val address: String,
    val city: String,
    val country: String,
    val phoneNumber: String?,
    val alternatePhoneNumber: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val logoUrl: String? = null,
)
