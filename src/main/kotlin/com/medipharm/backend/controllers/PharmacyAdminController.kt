package com.medipharm.backend.controllers

import com.medipharm.backend.entities.AddInventoryRequest
import com.medipharm.backend.entities.CreateMedication
import com.medipharm.backend.entities.InventoryItemDetailDto
import com.medipharm.backend.entities.InventoryStatsDto
import com.medipharm.backend.entities.OrderDto
import com.medipharm.backend.entities.PharmacyAnalyticsDto
import com.medipharm.backend.entities.PharmacyDashboardDto
import com.medipharm.backend.entities.PharmacyDetailDto
import com.medipharm.backend.entities.PharmacySearchInsightsDto
import com.medipharm.backend.entities.UpdateInventoryRequest
import com.medipharm.backend.entities.UpdatePharmacyRequest
import com.medipharm.backend.services.PharmacyAdminService
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/admin")
class PharmacyAdminController(
    private val pharmacyAdminService: PharmacyAdminService
) {
    // Dashboard
    @GetMapping("/dashboard_stats")
    @RateLimiter(name = "adminApi")
    fun getDashboardStats(@AuthenticationPrincipal userId: Long): Mono<PharmacyDashboardDto> {
        return pharmacyAdminService.getDashboardStats(userId)
    }

    @GetMapping("/dashboard_analytics")
    fun getAnalytics(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false, defaultValue = "7") days: Int
    ): Mono<PharmacyAnalyticsDto> {
        return pharmacyAdminService.getAnalytics(userId, days)
    }

    // Pharmacy Info
    @GetMapping("/pharmacy")
    fun getMyPharmacy(@AuthenticationPrincipal userId: Long): Mono<PharmacyDetailDto> {
        return pharmacyAdminService.getPharmacyByAdmin(userId)
    }

    @PutMapping("/pharmacy")
    fun updatePharmacy(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: UpdatePharmacyRequest
    ): Mono<PharmacyDetailDto> {
        return pharmacyAdminService.updatePharmacy(userId, request)
    }

    @PostMapping("/pharmacy/logo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadLogo(
        @AuthenticationPrincipal userId: Long,
        @RequestParam("file") file: MultipartFile
    ): Mono<String> {
        return pharmacyAdminService.uploadLogo(userId, file)
    }

    // Inventory Management
    @GetMapping("/inventory")
    fun getInventory(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) lowStock: Boolean?
    ): Flux<InventoryItemDetailDto> {
        return pharmacyAdminService.getInventory(userId, search, lowStock)
    }

    @GetMapping("/inventory/stats")
    fun getInventoryStats(@AuthenticationPrincipal userId: Long): Mono<InventoryStatsDto> {
        return pharmacyAdminService.getInventoryStats(userId)
    }

    @PostMapping("/inventory")
    fun addInventoryItem(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: AddInventoryRequest
    ): Mono<InventoryItemDetailDto> {
        return pharmacyAdminService.addInventoryItem(userId, request)
    }

    @PostMapping("/inventory/bulk")
    fun addBulkInventory(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: List<AddInventoryRequest>
    ): Flux<InventoryItemDetailDto> {
        return pharmacyAdminService.addBulkInventory(userId, request)
    }

    @PutMapping("/inventory/{itemId}")
    fun updateInventoryItem(
        @AuthenticationPrincipal userId: Long,
        @PathVariable itemId: Long,
        @Valid @RequestBody request: UpdateInventoryRequest
    ): Mono<InventoryItemDetailDto> {
        return pharmacyAdminService.updateInventoryItem(userId, itemId, request)
    }

    // Import
    @PostMapping("/bulk/medication")
    fun importMedications(
        @AuthenticationPrincipal userId: Long,
        @RequestBody medicationList: List<CreateMedication>
    ): Flux<InventoryItemDetailDto> {
        return pharmacyAdminService.addBulkMedications(userId, medicationList)
    }

    @PostMapping("/medication", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addMedication(
        @AuthenticationPrincipal userId: Long,
        @RequestParam medication: CreateMedication
    ): Mono<InventoryItemDetailDto> {
        return pharmacyAdminService.addMedication(userId, medication)
    }

    // Search Insights for this pharmacy
    @GetMapping("/search-insights")
    fun getSearchInsights(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(defaultValue = "30") days: Int
    ): Mono<PharmacySearchInsightsDto> {
        return pharmacyAdminService.getSearchInsights(userId, days)
    }

    // Orders (if implemented)
    @GetMapping("/orders")
    fun getOrders(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false) status: String?
    ): Flux<OrderDto> {
        return pharmacyAdminService.getOrders(userId, status)
    }
}