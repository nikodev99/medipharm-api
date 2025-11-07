package com.medipharm.backend.repository

import com.medipharm.backend.entities.Pharmacy
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PharmacyRepository: R2dbcRepository<Pharmacy, Long> {
    fun findByAdminId(adminId: Long): Mono<Pharmacy>
    fun findByIsActiveTrue(active: Boolean): Flux<Pharmacy>

    @Query("""
        SELECT * FROM pharmacies p WHERE p.is_active = true AND earth_distance(
            ll_to_earth(p.latitude, p.longitude),
            ll_to_earth(:lat, :lng)
        ) <= :radiusMeters ORDER BY earth_distance(
            ll_to_earth(p.latitude, p.longitude),
            ll_to_earth(:lat, :lng)
        ) LIMIT 50
    """)
    fun findNearbyPharmacy(lat: Double, lng: Double, radiusMeters: Double): Flux<Pharmacy>
}