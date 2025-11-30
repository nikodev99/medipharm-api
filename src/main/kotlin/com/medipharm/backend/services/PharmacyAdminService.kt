package com.medipharm.backend.services

import com.medipharm.backend.entities.ActivityDto
import com.medipharm.backend.entities.AddInventoryRequest
import com.medipharm.backend.entities.CreateMedication
import com.medipharm.backend.entities.InventoryItemDetailDto
import com.medipharm.backend.entities.InventoryStatsDto
import com.medipharm.backend.entities.Medication
import com.medipharm.backend.entities.OrderDto
import com.medipharm.backend.entities.PharmacyAnalyticsDto
import com.medipharm.backend.entities.PharmacyDashboardDto
import com.medipharm.backend.entities.PharmacyDetailDto
import com.medipharm.backend.entities.PharmacyInventory
import com.medipharm.backend.entities.PharmacySearchInsightsDto
import com.medipharm.backend.entities.PharmacyStatsDto
import com.medipharm.backend.entities.PopularMedicationDto
import com.medipharm.backend.entities.PriceComparisonDto
import com.medipharm.backend.entities.TopSearchDto
import com.medipharm.backend.entities.TrendDataPoint
import com.medipharm.backend.entities.UpdateInventoryRequest
import com.medipharm.backend.entities.UpdatePharmacyRequest
import com.medipharm.backend.entities.toDetailDto
import com.medipharm.backend.entities.toPharmacyAdminDto
import com.medipharm.backend.repository.MedicationRepository
import com.medipharm.backend.repository.PharmacyInventoryRepository
import com.medipharm.backend.repository.PharmacyRepository
import com.medipharm.backend.repository.SearchHistoryRepository
import com.medipharm.backend.repository.UserRepository
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class PharmacyAdminService(
    private val pharmacyRepository: PharmacyRepository,
    private val medicationRepository: MedicationRepository,
    private val pharmacyInventoryRepository: PharmacyInventoryRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val userRepository: UserRepository
) {
    @CircuitBreaker(name = "pharmacyAdmin")
    fun getDashboardStats(userId: Long): Mono<PharmacyDashboardDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                Mono.zip(
                    getPharmacyStats(pharmacy.id ?: 0),
                    getRecentActivity(pharmacy.id ?: 0),
                    getLowStockItems(pharmacy.id ?: 0)
                ).map { tuple ->
                    PharmacyDashboardDto(
                        pharmacy = pharmacy.toDetailDto(null, 0),
                        stats = tuple.t1,
                        recentActivity = tuple.t2,
                        lowStockItems = tuple.t3
                    )
                }
            }
    }

    fun getAnalytics(userId: Long, days: Int): Mono<PharmacyAnalyticsDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                Mono.zip(
                    getInventoryTrends(pharmacy.id!!, days),
                    getPopularMedications(pharmacy.id),
                    getSearchImpressions(pharmacy.id, days),
                    getPriceComparison(pharmacy.id)
                ).map { tuple ->
                    PharmacyAnalyticsDto(
                        inventoryTrends = tuple.t1,
                        popularMedications = tuple.t2,
                        searchImpressions = tuple.t3,
                        priceComparison = tuple.t4
                    )
                }
            }
    }

    fun getPharmacyByAdmin(userId: Long): Mono<PharmacyDetailDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                Mono.zip(
                    userRepository.findById(userId).map { it.toPharmacyAdminDto() },
                    pharmacyInventoryRepository.countByPharmacyId(pharmacy.id!!)
                ).map { tuple ->
                    pharmacy.toDetailDto(tuple.t1, tuple.t2.toInt())
                }
            }
    }

    @CacheEvict(value = ["pharmacies"], allEntries = true)
    fun updatePharmacy(userId: Long, request: UpdatePharmacyRequest): Mono<PharmacyDetailDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                val updated = pharmacy.copy(
                    name = request.name!!,
                    address = request.address,
                    phoneNumber = request.phoneNumber,
                    alternatePhoneNumber = request.alternatePhoneNumber,
                    latitude = request.latitude,
                    longitude = request.longitude,
                    description = request.description
                )

                pharmacyRepository.save(updated)
                    .flatMap { savedPharmacy ->
                        userRepository.findById(userId)
                            .flatMap { admin ->
                                pharmacyInventoryRepository.countByPharmacyId(savedPharmacy.id!!)
                                    .map { count ->
                                        savedPharmacy.toDetailDto(admin.toPharmacyAdminDto(), count.toInt())
                                    }
                            }
                    }
            }
    }

    fun uploadLogo(userId: Long, file: MultipartFile): Mono<String> {
        // Upload to cloud storage (S3, Cloudinary, etc.)
        // For now, return a mock URL
        return Mono.just("https://storage.medipharm.cg/logos/${userId}_${System.currentTimeMillis()}.png")
    }

    @Cacheable(value = ["inventory"], key = "#userId + '-' + #search + '-' + #lowStock")
    fun getInventory(userId: Long, search: String?, lowStock: Boolean?): Flux<InventoryItemDetailDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMapMany { pharmacy ->
                pharmacyInventoryRepository.findByPharmacyId(pharmacy.id!!)
                    .filter { item ->
                        (lowStock == null || (lowStock && (item.quantity ?: 0) < 10))
                    }
                    .flatMap { item ->
                        medicationRepository.findById(item.medicationId)
                            .mapNotNull { medication ->
                                if (search == null ||
                                    medication.name.contains(search, ignoreCase = true) ||
                                    medication.dci.contains(search, ignoreCase = true)) {
                                    item.toDetailDto(medication)
                                } else {
                                    null
                                }
                            }
                    }
                    .filter { it != null }
            }
    }

    fun getInventoryStats(userId: Long): Mono<InventoryStatsDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                pharmacyInventoryRepository.findByPharmacyId(pharmacy.id!!)
                    .collectList()
                    .map { inventory ->
                        val totalItems = inventory.size
                        val totalValue = inventory.sumOf { it.price?.times(it.quantity ?: 0) ?: 0.0 }
                        val avgItemValue = if (totalItems > 0) totalValue / totalItems else 0.0
                        val expiringCount = inventory.count { item ->
                            item.expiryDate?.isBefore(LocalDateTime.now().plusMonths(3)) == true
                        }

                        InventoryStatsDto(
                            totalItems = totalItems,
                            totalValue = totalValue,
                            averageItemValue = avgItemValue,
                            categoryBreakdown = emptyMap(), //TODO To be implemented
                            expiringCount = expiringCount
                        )
                    }
            }
    }

    @CacheEvict(value = ["medications"], allEntries = true)
    fun addMedication(userId: Long, request: CreateMedication): Mono<InventoryItemDetailDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                val medication = Medication(
                    name = request.name,
                    nameLowercase = request.name.lowercase(),
                    dci = request.dci,
                    dciLowercase = request.dci.lowercase(),
                    description = request.description,
                    dosage = request.dosage,
                    form = request.form,
                    manufacturer = request.manufacturer,
                    activeIngredients = request.activeIngredients,
                    imageUrls = request.imageUrls,
                    leafletUrl = request.leafletUrl,
                    requiresPrescription = request.requiresPrescription,
                    isActive = request.isActive
                )

                medicationRepository.existsByNameAndDciAndDosageAndForm(
                    medication.name,
                    medication.dci,
                    medication.dosage!!,
                    medication.form
                ).flatMap { exists ->
                    if (exists) {
                        Mono.error { IllegalStateException("Ce médicament existe déjà dans votre inventaire") }
                    }else {
                        medicationRepository.save(medication)
                            .flatMap { savedMedication ->
                                val inventory = PharmacyInventory(
                                    pharmacyId = pharmacy.id!!,
                                    medicationId = savedMedication.id!!,
                                    quantity = request.quantity,
                                    price = request.price,
                                    isAvailable = request.isAvailable
                                )

                                pharmacyInventoryRepository.save(inventory)
                                    .map { it.toDetailDto(savedMedication) }
                            }
                    }
                }

            }
    }

    @CacheEvict(value = ["medication"], allEntries = true)
    fun addBulkMedications(userId: Long, requests: List<CreateMedication>): Flux<InventoryItemDetailDto> {
        return Flux.fromIterable(requests)
            .flatMap { request ->
                addMedication(userId, request)
                    .onErrorResume { Mono.empty() } // Skip errors in bulk
            }
    }

    @CacheEvict(value = ["inventory"], allEntries = true)
    fun addInventoryItem(userId: Long, request: AddInventoryRequest): Mono<InventoryItemDetailDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                medicationRepository.findById(request.medicationId)
                    .flatMap { medication ->
                        // Check if already exists
                        pharmacyInventoryRepository.findByPharmacyIdAndMedicationId(
                            pharmacy.id!!,
                            medication.id!!
                        ).flatMap { existing ->
                            Mono.error<InventoryItemDetailDto>(
                                IllegalStateException("Ce médicament existe déjà dans votre inventaire")
                            )
                        }.switchIfEmpty(
                            Mono.defer {
                                val item = PharmacyInventory(
                                    pharmacyId = pharmacy.id,
                                    medicationId = medication.id,
                                    quantity = request.quantity,
                                    price = request.price,
                                    isAvailable = request.isAvailable
                                )

                                pharmacyInventoryRepository.save(item)
                                    .map { it.toDetailDto(medication) }
                            }
                        )
                    }
            }
    }

    @CacheEvict(value = ["inventory"], allEntries = true)
    fun addBulkInventory(userId: Long, requests: List<AddInventoryRequest>): Flux<InventoryItemDetailDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMapMany { pharmacy ->
                Flux.fromIterable(requests)
                    .flatMap { request ->
                        addInventoryItem(userId, request)
                            .onErrorResume { Mono.empty() } // Skip errors in bulk
                    }
            }
    }

    @CacheEvict(value = ["inventory"], allEntries = true)
    fun updateInventoryItem(userId: Long, itemId: Long, request: UpdateInventoryRequest): Mono<InventoryItemDetailDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                pharmacyInventoryRepository.findById(itemId)
                    .flatMap { item ->
                        if (item.pharmacyId != pharmacy.id) {
                            Mono.error(SecurityException("Accès non autorisé"))
                        } else {
                            val updated = item.copy(
                                quantity = request.quantity,
                                price = request.price,
                                isAvailable = request.isAvailable
                            )

                            pharmacyInventoryRepository.save(updated)
                                .flatMap { savedItem ->
                                    medicationRepository.findById(savedItem.medicationId)
                                        .map { medication ->
                                            savedItem.toDetailDto(medication)
                                        }
                                }
                        }
                    }
            }
    }

    // Search Insights
    fun getSearchInsights(userId: Long, days: Int): Mono<PharmacySearchInsightsDto> {
        return pharmacyRepository.findByAdminId(userId)
            .flatMap { pharmacy ->
                pharmacyInventoryRepository.findByPharmacyId(pharmacy.id!!)
                    .flatMap { item ->
                        medicationRepository.findById(item.medicationId)
                            .flatMap { medication ->
                                searchHistoryRepository.countBySearchQueryContainingIgnoreCase(medication.name)
                                    .map { count ->
                                        Pair(medication.name, count)
                                    }
                            }
                    }
                    .collectList()
                    .map { searches ->
                        val totalImpressions = searches.sumOf { it.second }
                        val topSearched = searches
                            .sortedByDescending { it.second }
                            .take(10)
                            .map { TopSearchDto(it.first, it.second, 0) }

                        PharmacySearchInsightsDto(
                            yourMedicationsSearched = searches.size.toLong(),
                            totalImpressions = totalImpressions,
                            conversionRate = 0.0, // Calculate based on actual orders
                            topSearchedYourMeds = topSearched,
                            missedOpportunities = emptyList() // Medications searched but not in inventory
                        )
                    }
            }
    }

    // Orders (placeholder)
    fun getOrders(userId: Long, status: String?): Flux<OrderDto> {
        return Flux.empty() // To be implemented when an order system is ready
    }

    private fun getPharmacyStats(pharmacyId: Long): Mono<PharmacyStatsDto> {
        return pharmacyInventoryRepository.findByPharmacyId(pharmacyId)
            .collectList()
            .flatMap { inventory ->
                val totalInventory = inventory.size
                val availableItems = inventory.count { it.isAvailable && (it.quantity ?: 0) > 0 }
                val lowStockItems = inventory.count { (it.quantity ?: 0) in 1..<10 }
                val outOfStockItems = inventory.count { it.quantity == 0 }
                val totalValue = inventory.sumOf { it.price?.times(it.quantity ?: 0) ?: 0.0 }

                pharmacyRepository.findById(pharmacyId)
                    .map { pharmacy ->
                        PharmacyStatsDto(
                            totalInventory = totalInventory,
                            availableItems = availableItems,
                            lowStockItems = lowStockItems,
                            outOfStockItems = outOfStockItems,
                            totalValue = totalValue,
                            searchImpressions = 0L, //TODO Calculate from search history
                            averageRating = pharmacy.averageRating ?: 0.0,
                            totalReviews = pharmacy.totalReviews
                        )
                    }
            }
    }

    private fun getRecentActivity(pharmacyId: Long): Mono<List<ActivityDto>> {
        return pharmacyInventoryRepository.findByPharmacyId(pharmacyId)
            .sort { a, b -> (b.lastUpdated ?: LocalDateTime.now()).compareTo(a.lastUpdated) }
            .take(5)
            .flatMap { item ->
                medicationRepository.findById(item.medicationId)
                    .map { medication ->
                        ActivityDto(
                            type = if (item.quantity == 0) "out_of_stock" else "updated",
                            description = "${medication.name} - Stock: ${item.quantity}",
                            timestamp = item.lastUpdated.toString()
                        )
                    }
            }
            .collectList()
    }

    private fun getLowStockItems(pharmacyId: Long): Mono<List<InventoryItemDetailDto>> {
        return pharmacyInventoryRepository.findByPharmacyId(pharmacyId)
            .filter { (it.quantity ?: 0) in 1..<10 }
            .flatMap { item ->
                medicationRepository.findById(item.medicationId)
                    .map { medication ->
                        item.toDetailDto(medication)
                    }
            }
            .take(10)
            .collectList()
    }

    private fun getInventoryTrends(pharmacyId: Long, days: Int): Mono<List<TrendDataPoint>> {
        // Simplified - in production, track historical data
        return pharmacyInventoryRepository.findByPharmacyId(pharmacyId)
            .count()
            .map { count ->
                (0 until days).map { day ->
                    TrendDataPoint(
                        date = LocalDateTime.now().minusDays(day.toLong()).toLocalDate().toString(),
                        value = count
                    )
                }.reversed()
            }
    }

    private fun getPopularMedications(pharmacyId: Long): Mono<List<PopularMedicationDto>> {
        return pharmacyInventoryRepository.findByPharmacyId(pharmacyId)
            .flatMap { item ->
                medicationRepository.findById(item.medicationId)
                    .map { medication ->
                        PopularMedicationDto(
                            medicationId = medication.id!!,
                            name = medication.name,
                            impressions = 0L, //TODO Calculate from search history
                            currentStock = item.quantity ?: 0,
                            averagePrice = item.price ?: 0.0
                        )
                    }
            }
            .take(10)
            .collectList()
    }

    private fun getSearchImpressions(pharmacyId: Long, days: Int): Mono<List<TrendDataPoint>> {
        return Mono.just(emptyList()) // To be implemented with search tracking
    }

    private fun getPriceComparison(pharmacyId: Long): Mono<List<PriceComparisonDto>> {
        return pharmacyInventoryRepository.findByPharmacyId(pharmacyId)
            .flatMap { item ->
                pharmacyInventoryRepository.findAvailableMedication(item.medicationId)
                    .collectList()
                    .flatMap { allInventory ->
                        if (allInventory.size > 1) {
                            val avgPrice = allInventory.mapNotNull { it.price }.average()
                            medicationRepository.findById(item.medicationId)
                                .map { medication ->
                                    PriceComparisonDto(
                                        medicationName = medication.name,
                                        yourPrice = item.price ?: 0.0,
                                        marketAverage = avgPrice,
                                        difference = item.price?.minus(avgPrice) ?: 0.0
                                    )
                                }
                        } else {
                            Mono.empty()
                        }
                    }
            }
            .take(10)
            .collectList()
    }
}