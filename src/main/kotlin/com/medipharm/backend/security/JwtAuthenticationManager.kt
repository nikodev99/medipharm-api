package com.medipharm.backend.security

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val jwtTokenProvider: JwtTokenProvider
): ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication?): Mono<Authentication?>? {
        val token = authentication?.credentials.toString()

        return jwtTokenProvider.validateToken(token)
            .flatMap { isValid ->
                if (isValid) {
                    jwtTokenProvider.getUserFromToken(token)
                        .zipWith(jwtTokenProvider.getRoleFromToken(token))
                        .map { tuple ->
                            val userId = tuple.t1
                            val role = tuple.t2
                            val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

                            UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                            )
                        }
                }else {
                    Mono.empty()
                }
            }
    }
}