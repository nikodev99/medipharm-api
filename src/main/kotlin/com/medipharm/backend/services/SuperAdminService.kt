package com.medipharm.backend.services

import com.medipharm.backend.entities.*
import com.medipharm.backend.repository.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
@Transactional
class SuperAdminService(
    private val pharmacyRepository: PharmacyRepository,
    private val userRepository: UserRepository,
    private val medicationRepository: MedicationRepository,
    private val pharmacyInventoryRepository: PharmacyInventoryRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Cacheable(value = ["dashboard-stats"], key = "'dashboard-stats'")
    fun getDashboardData(): Mono<DashboardStatsDto> {
        return Mono.zip(
            pharmacyRepository.count(),
            pharmacyRepository.findByIsActiveTrue().count(),
            medicationRepository.count(),
            userRepository.count(),
            userRepository.countByIsPremiumTrue(),
            searchHistoryRepository.count(),
            searchHistoryRepository.countBySearchedAtAfter(LocalDateTime.now().minusDays(1)),
        ).map { tuple -> DashboardStatsDto(
            totalPharmacies = tuple.t1.toInt(),
            activePharmacies = tuple.t2.toInt(),
            totalMedications = tuple.t3.toInt(),
            totalUsers = tuple.t4.toInt(),
            premiumUsers = tuple.t5.toInt(),
            totalSearches = tuple.t6,
            searchesToday = tuple.t7,
            averageResponseTime = 0.0
        ) }
    }

    fun getAnalytics(days: Int): Mono<AnalyticsDto> {
        val startDate = LocalDateTime.now().minusDays(days.toLong())

        return Mono.zip(
            getSearchTrends(startDate),
            getTopMedications(days),
            getPharmacyPerformance(),
            getUserGrowth(startDate)
        ).map { tuple -> AnalyticsDto(
            searchTrends = tuple.t1,
            topMedications = tuple.t2,
            pharmacyPerformance = tuple.t3,
            userGrowth = tuple.t4,
            revenueData = emptyList() //TODO to be implemented
        )}
    }

    @Cacheable(value = ["pharmacies"], key = "'all-pharmacies'")
    fun getAllPharmacies(search: String?, city: String?, status: String?): Flux<PharmacyDetailDto> {
        return pharmacyRepository.findAll()
            .filter { pharmacy ->
                (search == null || pharmacy.name.contains(search, ignoreCase = true)) &&
                (city == null || pharmacy.city.equals(city, ignoreCase = true)) &&
                (status == null ||
                        (status == "active" && pharmacy.isActive) ||
                        (status == "inactive" && !pharmacy.isActive) ||
                        (status == "vérified" && pharmacy.isVerified))
            }
            .flatMap { pharmacy ->
                Mono.zip(
                    userRepository.findById(pharmacy.adminId ?: 0)
                        .map { it.toPharmacyAdminDto() },
                    pharmacyInventoryRepository.countByPharmacyId(pharmacy.id ?: 0)
                ).map { tuple ->
                    pharmacy.toDetailDto(tuple.t1, tuple.t2.toInt())
                }
            }
    }

    fun getPharmacyDetails(id: Long): Mono<PharmacyDetailDto> {
        return pharmacyRepository.findById(id)
            .flatMap { pharmacy ->
                Mono.zip(
                    userRepository.findById(pharmacy.adminId ?: 0L).map { it.toPharmacyAdminDto() },
                    pharmacyInventoryRepository.countByPharmacyId(pharmacy.id ?: 0)
                ).map { tuple ->
                    pharmacy.toDetailDto(tuple.t1, tuple.t2.toInt())
                }
            }
    }

    @CacheEvict(value = ["pharmacies"], allEntries = true)
    fun createPharmacy(request: CreatePharmacyRequest): Mono<PharmacyDto> {
        val admin = User(
            email = request.email,
            password = passwordEncoder.encode(request.adminPassword),
            fullName = request.adminName,
            role = UserRole.PHARMACY_ADMIN,
            authProvider = AuthProvider.LOCAL,
            emailVerified = true
        )

        return userRepository.save(admin)
            .flatMap { savedUser ->
                val pharmacy = Pharmacy(
                    name = request.name,
                    address = request.address,
                    city = request.city,
                    phoneNumber = request.phoneNumber,
                    alternatePhoneNumber = request.alternatePhoneNumber,
                    email = request.pharmacyEmail,
                    latitude = request.latitude,
                    longitude = request.longitude,
                    description = request.description,
                    licenceNumber = request.licenseNumber,
                    adminId = savedUser.id,
                    isActive = true,
                    isVerified = true,
                    country = request.country ?: ""
                )

                pharmacyRepository.save(pharmacy)
                    .map { savedPharmacy ->
                        savedPharmacy.toDto()
                    }
            }
    }

    @CacheEvict(value = ["pharmacies"], allEntries = true)
    fun updatePharmacy(id: Long, request: UpdatePharmacyRequest): Mono<PharmacyDto> {
        return pharmacyRepository.findById(id)
            .flatMap { pharmacy ->
                val updatedRequest = pharmacy.copy(
                    name = request.name!!,
                    address = request.address,
                    phoneNumber = request.phoneNumber,
                    alternatePhoneNumber = request.alternatePhoneNumber,
                    latitude = request.latitude,
                    longitude = request.longitude,
                    description = request.description,
                    logoUrl = request.logoUrl
                )

                pharmacyRepository.save(updatedRequest)
                    .map { savedPharmacy ->
                        savedPharmacy.toDto()
                    }
            }
    }

    @CacheEvict(value = ["pharmacies"], allEntries = true)
    fun verifyPharmacy(id: Long): Mono<PharmacyDetailDto> {
        return pharmacyRepository.findById(id)
            .flatMap { pharmacy ->
                val verified = pharmacy.copy(isVerified = true)
                pharmacyRepository.save(verified)
                    .flatMap { savedPharmacy ->
                        userRepository.findById(savedPharmacy.adminId!!)
                            .flatMap { admin ->
                                pharmacyInventoryRepository.countByPharmacyId(savedPharmacy.id!!)
                                    .map { count ->
                                        savedPharmacy.toDetailDto(admin.toPharmacyAdminDto(), count.toInt())
                                    }
                            }
                    }
            }
    }

    @CacheEvict(value = ["pharmacies"], allEntries = true)
    fun togglePharmacyStatus(id: Long): Mono<PharmacyDetailDto> {
        return pharmacyRepository.findById(id)
            .flatMap { pharmacy ->
                val toggled = pharmacy.copy(isActive = !pharmacy.isActive)
                pharmacyRepository.save(toggled)
                    .flatMap { savedPharmacy ->
                        userRepository.findById(savedPharmacy.adminId!!)
                            .flatMap { admin ->
                                pharmacyInventoryRepository.countByPharmacyId(savedPharmacy.id!!)
                                    .map { count ->
                                        savedPharmacy.toDetailDto(admin.toPharmacyAdminDto(), count.toInt())
                                    }
                            }
                    }
            }
    }

    @CacheEvict(value = ["pharmacies"], allEntries = true)
    fun deletePharmacy(id: Long): Mono<Void> {
        return pharmacyRepository.deleteById(id)
    }

    fun createPharmacyAdmin(request: CreatePharmacyAdminRequest): Mono<PharmacyAdminDto> {
        val admin = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            role = UserRole.PHARMACY_ADMIN,
            authProvider = AuthProvider.LOCAL,
            emailVerified = true
        )

        return userRepository.save(admin)
            .flatMap { savedAdmin ->
                pharmacyRepository.findById(request.pharmacyId)
                    .flatMap { pharmacy ->
                        val updated = pharmacy.copy(adminId = savedAdmin.id!!)
                        pharmacyRepository.save(updated)
                            .map { savedAdmin.toPharmacyAdminDto(it.name) }
                    }
            }
    }

    fun getAllPharmacyAdmins(): Flux<PharmacyAdminDto> {
        return userRepository.findByRole(UserRole.PHARMACY_ADMIN.name)
            .flatMap { admin ->
                pharmacyRepository.findByAdminId(admin.id!!)
                    .map { pharmacy -> admin.toPharmacyAdminDto(pharmacy.name) }
                    .defaultIfEmpty(admin.toPharmacyAdminDto(null))
            }
    }

    fun toggleAdminStatus(id: Long): Mono<PharmacyAdminDto> {
        return userRepository.findById(id)
            .flatMap { admin ->
                val updated = admin.copy(isActive = !admin.isActive)
                userRepository.save(updated)
                    .flatMap { savedAdmin ->
                        pharmacyRepository.findByAdminId(savedAdmin.id!!)
                            .map { pharmacy -> savedAdmin.toPharmacyAdminDto(pharmacy.name) }
                            .defaultIfEmpty(savedAdmin.toPharmacyAdminDto(null))
                    }
            }
    }

    fun getAllMedications(search: String?): Flux<MedicationWithStatsDto> {
        return (if (search != null) {
            medicationRepository.searchMedication("%$search%", search.lowercase())
        } else {
            medicationRepository.findAll()
        }).flatMap { medication ->
            Mono.zip(
                searchHistoryRepository.countBySearchQueryContainingIgnoreCase(medication.name),
                pharmacyInventoryRepository.findAvailableMedication(medication.id!!).count(),
                pharmacyInventoryRepository.findAvailableMedication(medication.id)
                    .mapNotNull { it.price }
                    .collectList()
                    .mapNotNull { prices -> prices.filterNotNull().average().takeIf { it.isNaN() } }
            ).map { tuple ->
                medication.toWithStatsDto(tuple.t1, tuple.t2.toInt(), tuple.t3)
            }
        }
    }

    @CacheEvict(value = ["medications"], allEntries = true)
    fun createMedication(request: CreateMedicationRequest): Mono<MedicationDto> {
        val medication = Medication(
            name = request.name,
            nameLowercase = request.name.lowercase(),
            dci = request.dci,
            dciLowercase = request.dci.lowercase(),
            dosage = request.dosage,
            form = MedicationForm.valueOf(request.form.uppercase()),
            manufacturer = request.manufacturer,
            description = request.description,
            requiresPrescription = request.requiresPrescription,
            isActive = true
        )

        return medicationRepository.save(medication)
            .map { it.toDto() }
    }

    @CacheEvict(value = ["medications"], allEntries = true)
    fun updateMedication(id: Long, request: UpdateMedicationRequest): Mono<MedicationDto> {
        return medicationRepository.findById(id)
            .flatMap { medication ->
                val updated = medication.copy(
                    name = request.name,
                    nameLowercase = request.name.lowercase(),
                    dci = request.dci,
                    dciLowercase = request.dci.lowercase(),
                    dosage = request.dosage,
                    form = MedicationForm.valueOf(request.form.uppercase()),
                    manufacturer = request.manufacturer,
                    description = request.description,
                    isActive = request.isActive,
                    requiresPrescription = request.requiresPrescription
                )

                medicationRepository.save(updated)
                    .map { it.toDto() }
            }
    }

    @CacheEvict(value = ["medications"], allEntries = true)
    fun deleteMedication(id: Long): Mono<Void> {
        return medicationRepository.deleteById(id)
    }

    fun getSearchAnalytics(days: Int, limit: Int): Mono<SearchAnalyticsDto> {
        val startDate = LocalDateTime.now().minusDays(days.toLong())

        return searchHistoryRepository.findBySearchedAtAfter(startDate)
            .collectList()
            .flatMap { searches ->
                val totalSearches = searches.size.toLong()
                val uniqueSearches = searches.map { it.searchQuery.lowercase() }.distinct().size.toLong()
                val avgPerDay = totalSearches.toDouble() / days

                val topSearches = searches
                    .groupBy { it.searchQuery.lowercase() }
                    .map { (query, list) ->
                        TopSearchDto(
                            query = query,
                            count = list.size.toLong(),
                            resultsCount = list.firstOrNull()?.resultCount ?: 0
                        )
                    }
                    .sortedByDescending { it.count }
                    .take(limit)

                val noResultSearches = searches
                    .filter { it.resultCount == 0 }
                    .map { it.searchQuery }
                    .distinct()
                    .take(limit)

                Mono.just(
                    SearchAnalyticsDto(
                        totalSearches = totalSearches,
                        uniqueSearches = uniqueSearches,
                        averageSearchesPerDay = avgPerDay,
                        topSearches = topSearches,
                        searchTrends = emptyList(), //TODO To be calculated
                        noResultSearches = noResultSearches
                    )
                )
            }
    }

    fun getSearchHistory(page: Int, size: Int): Flux<SearchHistoryDto> {
        return searchHistoryRepository.findAll()
            .skip((page * size).toLong())
            .take(size.toLong())
            .flatMap { history ->
                if (history.userId != null) {
                    userRepository.findById(history.userId)
                        .map { user ->
                            SearchHistoryDto(
                                id = history.id!!,
                                userId = user.id,
                                userEmail = user.email,
                                searchQuery = history.searchQuery,
                                resultsCount = history.resultCount,
                                searchedAt = history.searchedAt.toString()
                            )
                        }
                } else {
                    Mono.just(
                        SearchHistoryDto(
                            id = history.id!!,
                            userId = null,
                            userEmail = null,
                            searchQuery = history.searchQuery,
                            resultsCount = history.resultCount,
                            searchedAt = history.searchedAt.toString()
                        )
                    )
                }
            }
    }

    fun getAllUsers(search: String?, role: String?, isPremium: Boolean?): Flux<UserDetailDto> {
        return userRepository.findAll()
            .filter { user ->
                (search == null ||
                        user.email.contains(search, ignoreCase = true) ||
                        user.fullName.contains(search, ignoreCase = true)) &&
                        (role == null || user.role.name == role.uppercase()) &&
                        (isPremium == null || user.isPremium == isPremium)
            }
            .map { it.toDetailDto() }
    }

    fun getUserStats(): Mono<UserStatsDto> {
        return userRepository.findAll()
            .collectList()
            .map { users ->
                val totalUsers = users.size
                val activeUsers = users.count { it.isActive }
                val premiumUsers = users.count { it.isPremium }
                val newUsersThisMonth = users.count {
                    it.createdAt.isAfter(LocalDateTime.now().minusMonths(1))
                }
                val usersByRole = users.groupBy { it.role.name }.mapValues { it.value.size }
                val usersByProvider = users.groupBy { it.authProvider.name }.mapValues { it.value.size }

                UserStatsDto(
                    totalUsers = totalUsers,
                    activeUsers = activeUsers,
                    premiumUsers = premiumUsers,
                    newUsersThisMonth = newUsersThisMonth,
                    usersByRole = usersByRole,
                    usersByProvider = usersByProvider
                )
            }
    }

    fun getSystemHealth(): Mono<SystemHealthDto> {
        return Mono.just(
            SystemHealthDto(
                status = "healthy",
                uptime = 0L, //TODO Get from actuator
                cpuUsage = 0.0,
                memoryUsage = 0.0,
                databaseConnections = 0,
                cacheHitRate = 0.0,
                averageResponseTime = 0.0,
                errorRate = 0.0
            )
        )
    }

    private fun getSearchTrends(startDate: LocalDateTime): Mono<List<TrendDataPoint>> {
        return searchHistoryRepository.findBySearchedAtAfter(startDate)
            .groupBy { it.searchedAt.toLocalDate().toString() }
            .flatMap { group ->
                group.count().map { count -> TrendDataPoint(
                    date = group.key(), value = count
                ) }
            }
            .collectList()
            .map { it.sortedBy { point -> point.date } }
    }

    private fun getTopMedications(days: Int): Mono<List<TopMedicationDto>> {
        return searchHistoryRepository.findAll()
            .filter { it.searchedAt.isAfter(LocalDateTime.now().minusDays(days.toLong())) }
            .collectMultimap { it.searchQuery.lowercase() }
            .flatMapMany { map ->
                Flux.fromIterable(map.entries.sortedByDescending { it.value.size }).take(10)
            }
            .flatMap { entry ->
                medicationRepository.searchMedication("%${entry.key}%", entry.key)
                    .next()
                    .flatMap { medication ->
                        if (medication.id == null) return@flatMap Mono.empty()
                        pharmacyInventoryRepository.countAvailableMedicationId(medication.id)
                            .map { count ->
                                TopMedicationDto(
                                    medicationId = medication.id,
                                    name = medication.name,
                                    searchCount = entry.value.size.toLong(),
                                    availablePharmacies = count.toInt()
                                )
                            }
                    }
            }
            .collectList()
    }

    private fun getPharmacyPerformance(): Mono<List<PharmacyPerformanceDto>> {
        return pharmacyRepository.findByIsActiveTrue()
            .flatMap { pharmacy ->
                Mono.zip(
                    pharmacyInventoryRepository.countByPharmacyId(pharmacy.id ?: 0),
                    pharmacyInventoryRepository.countAvailableByPharmacyId(pharmacy.id ?: 0),
                    pharmacyInventoryRepository.findByPharmacyId(pharmacy.id ?: 0)
                        .mapNotNull { it.price }
                        .collectList()
                        .map { prices -> prices.filterNotNull().average().takeIf { it.isNaN() } ?: 0.0 }
                ).map { tuple ->
                    PharmacyPerformanceDto(
                        pharmacyId = pharmacy.id ?: 0,
                        name = pharmacy.name,
                        totalInventory = tuple.t1.toInt(),
                        availableItems = tuple.t2.toInt(),
                        searchImpressions = 0L, //TODO To calculate from search history
                        averagePrice = tuple.t3
                    )
                }
            }
            .collectList()
    }

    private fun getUserGrowth(startDate: LocalDateTime): Mono<List<TrendDataPoint>> {
        return userRepository.findByCreatedAtAfter(startDate)
            .groupBy { it.createdAt.toLocalDate().toString() }
            .flatMap { group ->
                group.count().map { count ->
                    TrendDataPoint(date = group.key(), value = count)
                }
            }
            .collectList()
            .map { it.sortedBy { point -> point.date } }
    }
}