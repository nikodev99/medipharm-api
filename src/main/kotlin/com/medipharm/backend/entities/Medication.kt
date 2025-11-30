package com.medipharm.backend.entities

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("medications")
data class Medication(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("name")
    @field:NotBlank(message = "name must not be blank")
    val name: String,

    @Column("name_lowercase")
    val nameLowercase: String,

    @Column("dci")
    @field:NotBlank(message = "dci must not be blank")
    val dci: String,

    @Column("dci_lowercase")
    val dciLowercase: String,

    @Column("description")
    val description: String? = null,

    @Column("dosage")
    val dosage: String? = null,

    @Column("form")
    val form: MedicationForm,

    @Column("manufacturer")
    val manufacturer: String? = null,

    @Column("active_ingredients")
    val activeIngredients: List<String>? = null,

    @Column("image_urls")
    val imageUrls: List<String>? = null,

    @Column("leaflet_url")
    val leafletUrl: String? = null,

    @Column("requires_prescription")
    val requiresPrescription: Boolean = false,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("search_count")
    val searchCount: Int = 0,

    @Column("create_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("update_at")
    val updatedAt: LocalDateTime? = LocalDateTime.now()
)

data class MedicationDto(
    val id: Long? = null,
    val name: String,
    val dci: String,
    val description: String? = null,
    val dosage: String? = null,
    val form: MedicationForm,
    val manufacturer: String? = null,
    val activeIngredients: List<String>? = null,
    val imageUrls: List<String>? = null,
    val requiresPrescription: Boolean = false,
)

fun Medication.toDto(): MedicationDto = MedicationDto(
    id = this.id,
    name = this.name,
    dci = this.dci,
    description = this.description,
    dosage = this.dosage,
    form = this.form,
    manufacturer = this.manufacturer,
    activeIngredients = this.activeIngredients,
    imageUrls = this.imageUrls,
    requiresPrescription = this.requiresPrescription,
)

fun Medication.toWithStatsDto(totalSearches: Long, availableIn: Int, avgPrice: Double?) = MedicationWithStatsDto(
    id = id!!,
    name = name,
    dci = dci,
    dosage = dosage ?: "N/A",
    form = form.name,
    manufacturer = manufacturer,
    isActive = isActive,
    totalSearches = totalSearches,
    availableInPharmacies = availableIn,
    averagePrice = avgPrice,
    createdAt = createdAt.toString()
)

data class MedicationSearchResult(
    val medication: MedicationDto,
    val availableAt: List<PharmacyAvailability>
)

enum class MedicationForm {
    TABLET, CAPSULE, INJECTION, DROPS, OINTMENT, SYRUP, CREAM, POWDER, SPRAY, INHALER, SUPPOSITORY, OTHER
}
