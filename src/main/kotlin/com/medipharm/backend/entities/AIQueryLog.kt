package com.medipharm.backend.entities

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("ai_query_logs")
data class AIQueryLog(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("medication_id")
    val medicationId: Long,

    @Column("query")
    val query: String,

    @Column("response")
    val response: String,

    @Column("tokens_used")
    val tokensUsed: Int = 0,

    @CreatedDate
    @Column("createdAt")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
