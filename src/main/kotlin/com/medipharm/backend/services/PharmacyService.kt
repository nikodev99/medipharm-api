package com.medipharm.backend.services

import com.medipharm.backend.entities.PharmacyDto
import com.medipharm.backend.entities.toDto
import com.medipharm.backend.repository.PharmacyRepository
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PharmacyService(
    private val pharmacyRepository: PharmacyRepository
) {
    @Cacheable(value = ["pharmacies"], key = "'all-active'")
    @CircuitBreaker(name = "pharmacyService")
    fun getAllActivePharmacies(): Flux<PharmacyDto> {
        return pharmacyRepository.findByIsActiveTrue()
            .map { it.toDto() }
    }

    @Cacheable(value = ["pharmacies"], key = "#id")
    @CircuitBreaker(name = "pharmacyService")
    fun getPharmacyById(id: Long): Mono<PharmacyDto> {
        return pharmacyRepository.findById(id)
            .map { it.toDto() }
    }

    @Cacheable(value = ["pharmacies"], key = "'nearby-' + #latitude + '-' + #longitude + '-' + #radiusKm")
    @CircuitBreaker(name = "pharmacyService")
    fun getNearbyPharmacies(latitude: Double, longitude: Double, radiusKm: Double): Flux<PharmacyDto> {
        val radiusMeters = radiusKm * 1000
        return pharmacyRepository.findNearbyPharmacy(latitude, longitude, radiusMeters)
            .map { it.toDto() }
    }
}