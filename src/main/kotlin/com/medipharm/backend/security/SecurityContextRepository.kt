package com.medipharm.backend.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepository(
    private val authenticationManager: JwtAuthenticationManager
): ServerSecurityContextRepository {
    override fun save(
        exchange: ServerWebExchange?,
        context: SecurityContext?
    ): Mono<Void?>? {
        throw UnsupportedOperationException("Not supported - stateless")
    }

    override fun load(exchange: ServerWebExchange?): Mono<SecurityContext?>? {
        val authHeader = exchange?.request?.headers?.getFirst(HttpHeaders.AUTHORIZATION)

        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            val auth = UsernamePasswordAuthenticationToken(token, token)

            authenticationManager.authenticate(auth)
                ?.map { authentication -> SecurityContextImpl(authentication) as SecurityContext }
                ?.onErrorResume { Mono.empty() }
        }else {
            Mono.empty()
        }
    }
}