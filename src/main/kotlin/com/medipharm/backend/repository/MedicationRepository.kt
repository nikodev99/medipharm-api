package com.medipharm.backend.repository

import com.medipharm.backend.entities.Medication
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MedicationRepository: R2dbcRepository<Medication, String> {
    @Query("""
        SELECT m.* FROM medications m
        WHERE m.is_active = true
        AND (
            m.name_lowercase ILIKE :query
            OR m.dci_lowercase ILIKE :query
            OR similarity(m.name_lowercase, :queryLower) > 0.3
            OR similarity(m.dci_lowercase, :queryLower) > 0.3
        )
        ORDER BY 
            CASE 
                WHEN m.name_lowercase = :queryLower THEN 1
                WHEN m.dci_lowercase = :queryLower THEN 2
                WHEN m.name_lowercase LIKE :query THEN 3
                ELSE 4
            END,
            m.search_count DESC
        LIMIT 50
    """)
    fun searchMedication(query: String, queryLower: String): Flux<Medication>

    fun searchByIsActiveTrue(): Flux<Medication>

    @Query("SELECT m.* FROM medications m WHERE m.id = :medicationId")
    fun findById(medicationId: Long): Mono<Medication>

    @Modifying
    @Query("UPDATE medications SET search_count = search_count + 1 WHERE id = :id")
    fun incrementSearchCount(id: Long): Mono<Void>
}