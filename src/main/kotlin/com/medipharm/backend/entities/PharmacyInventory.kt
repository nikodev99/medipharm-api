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

data class PharmacyInventoryDto(
    val id: Long? = null,
    val medication: MedicationDto? = null,
    val quantity: Int? = null,
    val price: Double? = 0.0,
    val isAvailable: Boolean = true,
    val expiryDate: LocalDateTime? = null,
)

fun PharmacyInventory.toDto(): PharmacyInventoryDto = PharmacyInventoryDto(
    id = this.id,
    medication = MedicationDto(
        id = this.id,
        name = "",
        dci = "",
        description = "",
        dosage = "",
        form = MedicationForm.OTHER,
        manufacturer = "",
        activeIngredients = listOf(),
        imageUrls = listOf(),
        requiresPrescription = false,
    ),
    quantity = this.quantity,
    price = this.price,
    isAvailable = this.isAvailable,
    expiryDate = this.expiryDate,
)

fun PharmacyInventory.toDetailDto(medication: Medication) = InventoryItemDetailDto(
    id = id!!,
    medication = medication.toDto(),
    quantity = quantity ?: 0,
    price = price ?: 0.0,
    isAvailable = isAvailable,
    expiryDate = expiryDate?.toString(),
    lastUpdated = lastUpdated.toString(),
    status = when {
        quantity == 0 -> "out-of-stock"
        (quantity ?: 0) < 10 -> "low-stock"
        else -> "in-stock"
    }
)

data class AddInventoryRequest(
    val pharmacyId: Long,
    val medicationId: Long,
    val quantity: Int? = null,
    val price: Double? = 0.0,
    val isAvailable: Boolean = true,
    val expiryDate: LocalDateTime? = null,
)
