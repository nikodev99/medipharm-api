package com.medipharm.backend.config

import com.medipharm.backend.security.JwtAuthenticationManager
import com.medipharm.backend.security.SecurityContextRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationManager: JwtAuthenticationManager,
    private val securityContextRepository: SecurityContextRepository
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/auth/**", "/public/**").permitAll()
                    .pathMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                    .pathMatchers("/admin/**").hasRole("PHARMACY_ADMIN")
                    .pathMatchers("/premium/**").authenticated()
                    .pathMatchers(HttpMethod.GET, "/search/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/pharmacies/**").permitAll()
                    .anyExchange().authenticated()
            }
            .authenticationManager(jwtAuthenticationManager)
            .securityContextRepository(securityContextRepository)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)
}