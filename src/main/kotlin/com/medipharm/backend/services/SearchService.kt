package com.medipharm.backend.services

import com.medipharm.backend.entities.MedicationSearchResult
import com.medipharm.backend.repository.MedicationRepository
import com.medipharm.backend.repository.PharmacyInventoryRepository
import com.medipharm.backend.repository.PharmacyRepository
import com.medipharm.backend.repository.SearchHistoryRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class SearchService(
    private val medicationRepository: MedicationRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val pharmacyInventoryRepository: PharmacyInventoryRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) {
    fun searchMedication(
        query: String,
        latitude: Double?,
        longitude: Double?,
        userId: Long?
    ): Flux<MedicationSearchResult> {

    }
}