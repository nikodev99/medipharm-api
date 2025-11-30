package com.medipharm.backend.controllers

import com.medipharm.backend.entities.PharmacyDto
import com.medipharm.backend.services.PharmacyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/pharmacy")
class PharmacyController(private val pharmacyService: PharmacyService) {
    @GetMapping(value = ["", "/all"])
    fun getAllPharmacies(): Flux<PharmacyDto> {
        return pharmacyService.getAllActivePharmacies()
    }

    @GetMapping("/{id}")
    fun getPharmacy(@PathVariable id: Long): Mono<PharmacyDto> = pharmacyService.getPharmacyById(id)

    @GetMapping("/nearby")
    fun getNearbyPharmacies(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam radiusKm: Double
    ): Flux<PharmacyDto> {
        return pharmacyService.getNearbyPharmacies(latitude, longitude, radiusKm)
    }
}