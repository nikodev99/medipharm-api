package com.medipharm.backend.controllers;

import com.medipharm.backend.entities.AnalyticsDto
import com.medipharm.backend.entities.CreateMedicationRequest
import com.medipharm.backend.entities.CreatePharmacyAdminRequest
import com.medipharm.backend.entities.CreatePharmacyRequest
import com.medipharm.backend.entities.DashboardStatsDto
import com.medipharm.backend.entities.MedicationDto
import com.medipharm.backend.entities.MedicationWithStatsDto
import com.medipharm.backend.entities.PharmacyAdminDto
import com.medipharm.backend.entities.PharmacyDetailDto
import com.medipharm.backend.entities.PharmacyDto
import com.medipharm.backend.entities.SearchAnalyticsDto
import com.medipharm.backend.entities.SearchHistoryDto
import com.medipharm.backend.entities.SystemHealthDto
import com.medipharm.backend.entities.UpdateMedicationRequest
import com.medipharm.backend.entities.UpdatePharmacyAdminRequest
import com.medipharm.backend.entities.UpdatePharmacyRequest
import com.medipharm.backend.entities.UserDetailDto
import com.medipharm.backend.entities.UserStatsDto
import com.medipharm.backend.services.SuperAdminService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/superadmin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController(
        private val superAdminService: SuperAdminService
) {
    @GetMapping("/dashboard_stats")
    @RateLimiter(name = "adminApi")
    fun getDashboardStats(): Mono<DashboardStatsDto> {
        return superAdminService.getDashboardData()
    }

    @GetMapping("/dashboard_analytics")
    @RateLimiter(name = "adminApi")
    fun getAnalytics(
        @RequestParam(defaultValue = "7") days: Int
    ): Mono<AnalyticsDto> {
        return superAdminService.getAnalytics(days)
    }

    @GetMapping("/pharmacies")
    fun getAllPharmacies(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) status: String?
    ): Flux<PharmacyDetailDto> {
        return superAdminService.getAllPharmacies(search, city, status)
    }

    @GetMapping("/pharmacies/{id}")
    fun getPharmacyDetails(@PathVariable id: Long): Mono<PharmacyDetailDto> {
        return superAdminService.getPharmacyDetails(id)
    }

    @PostMapping("/pharmacies")
    fun createPharmacy(@Valid @RequestBody request: CreatePharmacyRequest): Mono<PharmacyDto> {
        return superAdminService.createPharmacy(request)
    }

    @PutMapping("/pharmacies/{id}")
    fun updatePharmacy(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePharmacyRequest
    ): Mono<PharmacyDto> {
        return superAdminService.updatePharmacy(id, request)
    }

    @PutMapping("/pharmacies/{id}/verify")
    fun verifyPharmacy(@PathVariable id: Long): Mono<PharmacyDetailDto> {
        return superAdminService.verifyPharmacy(id)
    }

    @PutMapping("/pharmacies/{id}/status")
    fun togglePharmacyStatus(@PathVariable id: Long): Mono<PharmacyDetailDto> {
        return superAdminService.togglePharmacyStatus(id)
    }

    @DeleteMapping("/pharmacies/{id}")
    fun deletePharmacy(@PathVariable id: Long): Mono<Void> {
        return superAdminService.deletePharmacy(id)
    }

    @PostMapping("/pharmacy-admins")
    fun createPharmacyAdmin(
        @Valid @RequestBody request: CreatePharmacyAdminRequest
    ): Mono<PharmacyAdminDto> {
        return superAdminService.createPharmacyAdmin(request)
    }

    @GetMapping("/pharmacy-admins")
    fun getAllPharmacyAdmins(): Flux<PharmacyAdminDto> {
        return superAdminService.getAllPharmacyAdmins()
    }

    @PutMapping("/pharmacy-admins/{id}/status")
    fun toggleAdminStatus(@PathVariable id: Long): Mono<PharmacyAdminDto> {
        return superAdminService.toggleAdminStatus(id)
    }

    @GetMapping("/medications")
    fun getAllMedications(
        @RequestParam(required = false) search: String?
    ): Flux<MedicationWithStatsDto> {
        return superAdminService.getAllMedications(search)
    }

    @PostMapping("/medications")
    fun createMedication(@Valid @RequestBody request: CreateMedicationRequest): Mono<MedicationDto> {
        return superAdminService.createMedication(request)
    }

    @PutMapping("/medications/{id}")
    fun updateMedication(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMedicationRequest
    ): Mono<MedicationDto> {
        return superAdminService.updateMedication(id, request)
    }

    @DeleteMapping("/medications/{id}")
    fun deleteMedication(@PathVariable id: Long): Mono<Void> {
        return superAdminService.deleteMedication(id)
    }

    // Search Analytics
    @GetMapping("/search-analytics")
    fun getSearchAnalytics(
        @RequestParam(defaultValue = "30") days: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): Mono<SearchAnalyticsDto> {
        return superAdminService.getSearchAnalytics(days, limit)
    }

    @GetMapping("/search-history")
    fun getSearchHistory(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): Flux<SearchHistoryDto> {
        return superAdminService.getSearchHistory(page, size)
    }

    // User Management
    @GetMapping("/users")
    fun getAllUsers(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) isPremium: Boolean?
    ): Flux<UserDetailDto> {
        return superAdminService.getAllUsers(search, role, isPremium)
    }

    @GetMapping("/users/stats")
    fun getUserStats(): Mono<UserStatsDto> {
        return superAdminService.getUserStats()
    }

    // System Health
    @GetMapping("/system/health")
    fun getSystemHealth(): Mono<SystemHealthDto> {
        return superAdminService.getSystemHealth()
    }
}
