package com.medipharm.backend.repository

import com.medipharm.backend.entities.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface UserRepository: R2dbcRepository<User, Long> {
    fun findByEmail(email: String): Mono<User>
    fun existsByEmail(email: String): Mono<Boolean>

    @Query("SELECT COUNT(u.id) FROM users u WHERE u.is_premium = true")
    fun countByIsPremiumTrue(): Mono<Long>

    fun findByCreatedAtAfter(date: LocalDateTime): Flux<User>

    @Query("SELECT u.* FROM users u WHERE u.role = :role")
    fun findByRole(role: String): Flux<User>
}