package com.medipharm.backend.repository

import com.medipharm.backend.entities.SearchHistory
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface SearchHistoryRepository: R2dbcRepository<SearchHistory, Long> {
    @Query("SELECT sh.* FROM search_history sh WHERE sh.user_id = :userId ORDER BY sh.searched_at desc LIMIT 100")
    fun findByUserIdOrderBySearchAtDesc(userId: Long): Flux<SearchHistory>

    fun countBySearchedAtAfter(searchedAtAfter: LocalDateTime): Mono<Long>
    fun countBySearchQueryContainingIgnoreCase(query: String): Mono<Long>
    fun findBySearchedAtAfter(searchedAtAfter: LocalDateTime): Flux<SearchHistory>
}