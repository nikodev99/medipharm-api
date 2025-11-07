package com.medipharm.backend.entities

import jakarta.validation.constraints.DecimalMin
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

@Table("pharmacy_inventories")
data class PharmacyInventory(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("pharmacy_id")
    val pharmacyId: Long,

    @Column("medication_id")
    val medicationId: Long,

    @Column("quantity")
    val quantity: Int? = null,

    @Column("price")
    @field:DecimalMin(value = "0.0", message = "price must be greater than or equal to zero")
    val price: Double? = 0.0,

    @Column("is_available")
    val isAvailable: Boolean = true,

    @Column("expiry_date")
    val expiryDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column("last_updated")
    val lastUpdated: LocalDateTime? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
