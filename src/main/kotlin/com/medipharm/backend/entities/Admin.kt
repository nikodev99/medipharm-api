package com.medipharm.backend.entities

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ImportResult(
    val imported: Int,
    val failed: Int,
    val errors: List<String>
)

/**
 * ***********************************
 *  Super Admin DTOs
 *  **********************************
 */

data class DashboardStatsDto(
    val totalPharmacies: Int,
    val activePharmacies: Int,
    val totalMedications: Int,
    val totalUsers: Int,
    val premiumUsers: Int,
    val totalSearches: Long,
    val searchesToday: Long,
    val averageResponseTime: Double
)

data class AnalyticsDto(
    val searchTrends: List<TrendDataPoint>,
    val topMedications: List<TopMedicationDto>,
    val pharmacyPerformance: List<PharmacyPerformanceDto>,
    val userGrowth: List<TrendDataPoint>,
    val revenueData: List<RevenueDataPoint>
)

data class TrendDataPoint(
    val date: String,
    val value: Long
)

data class TopMedicationDto(
    val medicationId: Long,
    val name: String,
    val searchCount: Long,
    val availablePharmacies: Int
)

data class PharmacyPerformanceDto(
    val pharmacyId: Long,
    val name: String,
    val totalInventory: Int,
    val availableItems: Int,
    val searchImpressions: Long,
    val averagePrice: Double
)

data class RevenueDataPoint(
    val date: String,
    val revenue: Double,
    val subscriptions: Int
)

data class PharmacyDetailDto(
    val id: Long,
    val name: String,
    val address: String,
    val city: String,
    val phoneNumber: String,
    val alternatePhoneNumber: String?,
    val email: String?,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val logoUrl: String?,
    val isActive: Boolean,
    val isVerified: Boolean,
    val licenseNumber: String?,
    val averageRating: Double,
    val totalReviews: Int,
    val admin: PharmacyAdminDto?,
    val inventoryCount: Int,
    val createdAt: String,
    val updatedAt: String
)

data class CreatePharmacyRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val address: String,
    val country: String?,
    val city: String = "Brazzaville",
    val phoneNumber: String,
    val alternatePhoneNumber: String?,
    val email: String,
    val pharmacyEmail: String?,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val licenseNumber: String?,
    val adminEmail: String,
    val adminName: String,
    val adminPassword: String
)

data class UpdatePharmacyAdminRequest(
    val name: String,
    val address: String,
    val phoneNumber: String,
    val alternatePhoneNumber: String?,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val isActive: Boolean
)

data class CreatePharmacyAdminRequest(
    @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank val fullName: String,
    val phoneNumber: String?,
    val pharmacyId: Long
)

data class PharmacyAdminDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val isActive: Boolean,
    val pharmacyId: Long?,
    val pharmacyName: String?,
    val createdAt: String
)

data class MedicationWithStatsDto(
    val id: Long,
    val name: String,
    val dci: String,
    val dosage: String,
    val form: String,
    val manufacturer: String?,
    val isActive: Boolean,
    val totalSearches: Long,
    val availableInPharmacies: Int,
    val averagePrice: Double?,
    val createdAt: String
)

data class CreateMedicationRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val dci: String,
    @field:NotBlank val dosage: String,
    val form: String,
    val manufacturer: String?,
    val description: String?,
    val requiresPrescription: Boolean = false
)

data class UpdateMedicationRequest(
    val name: String,
    val dci: String,
    val dosage: String,
    val form: String,
    val manufacturer: String?,
    val description: String?,
    val isActive: Boolean,
    val requiresPrescription: Boolean
)

data class SearchAnalyticsDto(
    val totalSearches: Long,
    val uniqueSearches: Long,
    val averageSearchesPerDay: Double,
    val topSearches: List<TopSearchDto>,
    val searchTrends: List<TrendDataPoint>,
    val noResultSearches: List<String>
)

data class TopSearchDto(
    val query: String,
    val count: Long,
    val resultsCount: Int
)

data class SearchHistoryDto(
    val id: Long,
    val userId: Long?,
    val userEmail: String?,
    val searchQuery: String,
    val resultsCount: Int,
    val searchedAt: String
)

data class UserDetailDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: String,
    val isPremium: Boolean,
    val premiumExpiryDate: String?,
    val isActive: Boolean,
    val emailVerified: Boolean,
    val authProvider: String,
    val createdAt: String,
    val lastActivity: String?
)

data class UserStatsDto(
    val totalUsers: Int,
    val activeUsers: Int,
    val premiumUsers: Int,
    val newUsersThisMonth: Int,
    val usersByRole: Map<String, Int>,
    val usersByProvider: Map<String, Int>
)

data class SystemHealthDto(
    val status: String,
    val uptime: Long,
    val cpuUsage: Double,
    val memoryUsage: Double,
    val databaseConnections: Int,
    val cacheHitRate: Double,
    val averageResponseTime: Double,
    val errorRate: Double
)

/**
 * ***********************************
 *  Pharmacy Admin DTOs
 *  **********************************
 */

data class PharmacyDashboardDto(
    val pharmacy: PharmacyDetailDto,
    val stats: PharmacyStatsDto,
    val recentActivity: List<ActivityDto>,
    val lowStockItems: List<InventoryItemDetailDto>
)

data class PharmacyStatsDto(
    val totalInventory: Int,
    val availableItems: Int,
    val lowStockItems: Int,
    val outOfStockItems: Int,
    val totalValue: Double,
    val searchImpressions: Long,
    val averageRating: Double,
    val totalReviews: Int
)

data class ActivityDto(
    val type: String,
    val description: String,
    val timestamp: String
)

data class PharmacyAnalyticsDto(
    val inventoryTrends: List<TrendDataPoint>,
    val popularMedications: List<PopularMedicationDto>,
    val searchImpressions: List<TrendDataPoint>,
    val priceComparison: List<PriceComparisonDto>
)

data class PopularMedicationDto(
    val medicationId: Long,
    val name: String,
    val impressions: Long,
    val currentStock: Int,
    val averagePrice: Double
)

data class PriceComparisonDto(
    val medicationName: String,
    val yourPrice: Double,
    val marketAverage: Double,
    val difference: Double
)

data class InventoryItemDetailDto(
    val id: Long,
    val medication: MedicationDto,
    val quantity: Int,
    val price: Double,
    val isAvailable: Boolean,
    val expiryDate: String?,
    val lastUpdated: String,
    val status: String // "in-stock", "low-stock", "out-of-stock"
)

data class CreateMedication(
    val name: String,
    val dci: String,
    val dosage: String,
    val form: MedicationForm,
    val manufacturer: String?,
    val description: String?,
    val requiresPrescription: Boolean = false,
    val activeIngredients: List<String> = listOf(),
    val imageUrls: List<String> = listOf(),
    val leafletUrl: String? = null,
    val isActive: Boolean = true,
    val quantity: Int? = 0,
    val price: Double? = 0.0,
    val isAvailable: Boolean = true,
    val expiryDate: LocalDate? = null
)

data class InventoryStatsDto(
    val totalItems: Int,
    val totalValue: Double,
    val averageItemValue: Double,
    val categoryBreakdown: Map<String, Int>,
    val expiringCount: Int
)

data class UpdateInventoryRequest(
    val quantity: Int,
    val price: Double,
    val isAvailable: Boolean
)

data class PharmacySearchInsightsDto(
    val yourMedicationsSearched: Long,
    val totalImpressions: Long,
    val conversionRate: Double,
    val topSearchedYourMeds: List<TopSearchDto>,
    val missedOpportunities: List<String>
)

data class OrderDto(
    val id: Long,
    val orderNumber: String,
    val customerName: String,
    val medications: List<String>,
    val total: Double,
    val status: String,
    val createdAt: String
)
