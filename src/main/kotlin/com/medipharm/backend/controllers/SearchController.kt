package com.medipharm.backend.controllers

import com.medipharm.backend.entities.MedicationSearchResult
import com.medipharm.backend.services.SearchService
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/search")
class SearchController(private val searchService: SearchService) {

    @GetMapping("/")
    @RateLimiter(name = "searchApi")
    fun searchMedications(
        @RequestParam query: String,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @AuthenticationPrincipal userId: Long?
    ): Flux<MedicationSearchResult> {
        return searchService.searchMedication(query, latitude, longitude, userId)
    }

    @GetMapping("/{medicationId}/availability")
    fun getMedicationAvailability(
        @PathVariable medicationId: Long,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?
    ): Mono<MedicationSearchResult> {
        return searchService.getMedicationAvailability(medicationId, latitude, longitude)
    }
}