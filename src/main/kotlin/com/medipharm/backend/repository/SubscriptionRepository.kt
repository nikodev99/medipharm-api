package com.medipharm.backend.repository

import com.medipharm.backend.entities.Subscription
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository: R2dbcRepository<Subscription, Long> {
}