package com.medipharm.backend.repository

import com.medipharm.backend.entities.PharmacyInventory
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PharmacyInventoryRepository: R2dbcRepository<PharmacyInventory, Long> {
    @Query("SELECT pi.* FROM pharmacy_inventories pi WHERE pi.pharmacy_id = :pharmacyId AND pi.medication_id = :medicationId")
    fun findByPharmacyIdAndMedicationId(pharmacyId: Long, medicationId: Long): Mono<PharmacyInventory>

    @Query("""
        SELECT pi.* FROM pharmacy_inventories pi WHERE pi.medication_id = :medicationId
        AND pi.is_available = true AND pi.quantity > 0 LIMIT 100
    """)
    fun findAvailableMedicationId(medicationId: Long): Flux<PharmacyInventory>

    fun findByPharmacyId(pharmacyId: Long): Flux<PharmacyInventory>

    @Query("""
        SELECT COUNT(pi.id) FROM pharmacy_inventories pi WHERE pi.pharmacy_id = :pharmacyId AND pi.is_available = true
    """)
    fun countAvailableByPharmacyId(pharmacyId: Long): Mono<Long>
}