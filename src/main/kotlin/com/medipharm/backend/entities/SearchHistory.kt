package com.medipharm.backend.entities

import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("search_history")
data class SearchHistory (
    @Id
    val id: Long? = null,

    @Column("user_id")
    val userId: Long? = null,

    @Column("search_query")
    @field:NotBlank
    val searchQuery: String,

    @Column("result_count")
    val resultCount: Int = 0,

    @CreatedDate
    @Column("searched_at")
    val searchedAt: LocalDateTime = LocalDateTime.now()
)