package com.medipharm.backend.services

import com.medipharm.backend.entities.MedicationSearchResult
import com.medipharm.backend.entities.PharmacyAvailability
import com.medipharm.backend.entities.SearchHistory
import com.medipharm.backend.entities.toDto
import com.medipharm.backend.repository.MedicationRepository
import com.medipharm.backend.repository.PharmacyInventoryRepository
import com.medipharm.backend.repository.PharmacyRepository
import com.medipharm.backend.repository.SearchHistoryRepository
import io.github.resilience4j.bulkhead.annotation.Bulkhead
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class SearchService(
    private val medicationRepository: MedicationRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val pharmacyInventoryRepository: PharmacyInventoryRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) {
    @CircuitBreaker(name = "medicationSearch", fallbackMethod = "searchFallback")
    @RateLimiter(name = "searchApi")
    @Bulkhead(name = "searchService")
    @TimeLimiter(name = "medicationSearch")
    @Cacheable(value = ["search-results"], key = "#query + '-' + #latitude + '-' + #longitude")
    fun searchMedication(
        query: String,
        latitude: Double?,
        longitude: Double?,
        userId: Long?
    ): Flux<MedicationSearchResult> {
        val queryLower = query.lowercase().trim()
        val queryPattern = "%$queryLower%"

        return medicationRepository.searchMedication(queryPattern, queryLower)
            .flatMap { medication ->
                medicationRepository.incrementSearchCount(medication.id!!).subscribe()

                pharmacyInventoryRepository.findAvailableMedicationId(medication.id)
                    .flatMap { inventory ->
                        pharmacyRepository.findById(inventory.pharmacyId)
                            .map { pharmacy ->
                                val distance = if (latitude != null && longitude != null) {
                                    calculateDistance(
                                        latitude, longitude,
                                        pharmacy.latitude, pharmacy.longitude
                                    )
                                } else null

                                PharmacyAvailability(
                                    pharmacy = pharmacy.toDto(),
                                    price = inventory.price ?: 0.0,
                                    quantity = inventory.quantity ?: 0,
                                    distance = distance
                                )
                            }
                    }
                    .collectList()
                    .map { availabilities ->
                        MedicationSearchResult(
                            medication = medication.toDto(),
                            availableAt = availabilities.sortedBy { it.distance ?: Double.MAX_VALUE }
                        )
                    }
            }
            .doOnComplete {
                userId?.let {
                    searchHistoryRepository.save<SearchHistory>(SearchHistory(
                        userId = it,
                        searchQuery = query,
                        resultsCount = 0
                    )).subscribe()
                }
            }
    }

    @CircuitBreaker(name = "medicationSearch")
    @Cacheable(value = ["medications"], key = "#medicationId")
    fun getMedicationAvailability(
        medicationId: Long,
        latitude: Double?,
        longitude: Double?
    ): Mono<MedicationSearchResult> {
        return medicationRepository.findById(medicationId)
            .flatMap { medication ->
                pharmacyInventoryRepository.findAvailableMedicationId(medication.id as Long)
                    .flatMap { inventory ->
                        pharmacyRepository.findById(inventory.pharmacyId)
                            .map { pharmacy ->
                                val distance = if (latitude != null && longitude != null) {
                                    calculateDistance(latitude, longitude, pharmacy.latitude, pharmacy.longitude)
                                }else null

                                PharmacyAvailability(
                                    pharmacy = pharmacy.toDto(),
                                    price = inventory.price as Double,
                                    quantity = inventory.quantity as Int,
                                    distance = distance
                                )
                            }
                    }
                    .collectList()
                    .map { availabilities ->
                        MedicationSearchResult(
                            medication = medication.toDto(),
                            availableAt = availabilities.sortedBy { it.distance ?: Double.MAX_VALUE }
                        )
                    }
            }
    }

    private fun searchFallback(
        query: String,
        latitude: Double?,
        longitude: Double?,
        userId: Long?,
        ex: Exception
    ): Flux<MedicationSearchResult> {
        return Flux.empty()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}