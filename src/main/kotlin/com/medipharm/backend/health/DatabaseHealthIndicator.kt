package com.medipharm.backend.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class DatabaseHealthIndicator(
    private val databaseClient: DatabaseClient
): ReactiveHealthIndicator {
    override fun health(): Mono<Health?>? {
        return databaseClient.sql("SELECT 1")
            .fetch()
            .one()
            .map { Health.up().build() }
            .onErrorResume { Mono.just(Health.down().withException(it).build()) }
            .timeout(Duration.ofSeconds(2))
            .onErrorResume { Mono.just(Health.down().withDetail("raison", "timeout").build()) }
    }
}