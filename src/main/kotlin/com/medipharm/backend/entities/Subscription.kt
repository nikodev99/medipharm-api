package com.medipharm.backend.entities

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("subscriptions")
data class Subscription(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("subscription_plan")
    val plan: SubscriptionPLan,

    @Column("start_date")
    val startDate: LocalDateTime,

    @Column("end_date")
    val endDate: LocalDateTime,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("transaction_id")
    val transactionId: String? = null,

    @Column("payment_method")
    val paymentMethod: PaymentMethod? = null,

    @Column("amount")
    val amount: Double,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentMethod {
    AIRTEL_MONEY, MTN_MONEY, CREDIT_CARD
}

enum class SubscriptionPLan {
    MONTHLY, YEARLY
}
