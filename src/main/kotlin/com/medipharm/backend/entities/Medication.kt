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

    @Column("dci")
    @field:NotBlank(message = "dci must not be blank")
    val dci: String,

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

enum class MedicationForm {
    TABLET, CAPSULE, INJECTION, DROPS, OINTMENT, SYRUP, CREAM, POWDER, SPRAY, INHALER, SUPPOSITORY, OTHER
}
