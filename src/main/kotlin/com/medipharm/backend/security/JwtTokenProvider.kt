package com.medipharm.backend.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.Date

@Component
class JwtTokenProvider(
    @param:Value("\${jwt.secret}") private val jwtSecret: String,
    @param:Value("\${jwt.expiration}") private val jwtExpiration: Long,
    @param:Value("\${jwt.refresh-expiration}") private val jwtRefreshExpiration: Long,
) {
    private val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun generateToken(userId: Long, role: String): Mono<String> {
        return Mono.fromCallable {
            val now = Date()
            val expiryDate = Date(now.time + jwtExpiration)

            Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact()
        }
    }

    fun getUserFromToken(token: String): Mono<Long> {
        return Mono.fromCallable {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            claims.subject.toLong()
        }
    }

    fun getRoleFromToken(token: String): Mono<String> {
        return Mono.fromCallable {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            claims["role"] as String
        }
    }

    fun validateToken(token: String): Mono<Boolean> {
        return Mono.fromCallable {
            try {
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)

                true
            }catch (ex: JwtException) {
                false
            }
        }
    }
}