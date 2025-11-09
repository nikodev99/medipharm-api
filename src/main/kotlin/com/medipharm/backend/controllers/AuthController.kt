package com.medipharm.backend.controllers

import com.medipharm.backend.entities.*
import com.medipharm.backend.helper.RequestUtils
import com.medipharm.backend.repository.UserRepository
import com.medipharm.backend.security.JwtTokenProvider
import com.medipharm.backend.services.RefreshService
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider,
    private val refreshService: RefreshService,
) {
    @PostMapping("/signup")
    @RateLimiter(name="authApi")
    fun registerUser(@Valid @RequestBody request: SignupRequest, exchange: ServerWebExchange): Mono<AuthResponse> {
        return userRepository.existsByEmail(request.email)
            .flatMap { exists ->
                if (exists) {
                    Mono.error { ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists") }
                }else {
                    val user = User(
                        email = request.email,
                        password = passwordEncoder.encode(request.password),
                        fullName = request.fullName,
                        phoneNumber = request.phoneNumber,
                        role = UserRole.USER,
                        authProvider = AuthProvider.LOCAL
                    )

                    userRepository.save<User>(user)
                        .flatMap { savedUser ->
                            tokenProvider.generateToken(savedUser.id!!, savedUser.role.name)
                                .zipWith(tokenProvider.generateRefreshToken(savedUser.id))
                                .flatMap { tokens -> saveRefreshAndRespond(savedUser, tokens, exchange) }
                        }
                }
            }
    }

    @PostMapping("/login")
    @RateLimiter(name="authApi")
    fun login(@Valid @RequestBody request: LoginRequest, exchange: ServerWebExchange): Mono<AuthResponse> {
        return userRepository.findByEmail(request.email)
            .flatMap { user ->
                if (passwordEncoder.matches(request.password, user.password)) {
                    tokenProvider.generateToken(user.id!!, user.role.name)
                        .zipWith(tokenProvider.generateRefreshToken(user.id))
                        .flatMap { tokens -> saveRefreshAndRespond(user, tokens, exchange) }

                } else {
                    Mono.error { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials") }
                }
            }
            .switchIfEmpty(Mono.error { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials") })
    }

    @PostMapping("/refresh")
    @RateLimiter(name="authApi")
    fun refreshToken(@RequestBody body: Map<String, String>): Mono<AuthResponse> {
        val refreshToken = body["refreshToken"] ?: return Mono.error { ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required") }

        return refreshService.refreshTokenUsage(refreshToken)
            .flatMap { refresh ->
                userRepository.findById(refresh.userId)
                    .flatMap { user ->
                        tokenProvider.generateToken(user.id!!, user.role.name)
                            .map { token -> AuthResponse(token, refreshToken, user.toDto()) }
                    }
            }
    }

    private fun saveRefreshAndRespond(
        user: User,
        tokens: Tuple2<String, String>,
        exchange: ServerWebExchange
    ): Mono<AuthResponse> {
        val req = RequestUtils.fromRequest(exchange.request)
        println("REQUEST: $req")
        val expiryDate = tokenProvider.getRefreshExpiryDate().toInstant()
        return refreshService.saveRefreshToken(
            Refresh(
                userId = user.id!!,
                refreshToken = tokens.t2,
                userAgent = req.userAgent,
                clientIp = req.clientIp,
                expiryDate = expiryDate,
                deviceFingerprint = req.deviceFingerprint,
                clientType = req.clientType,
            )
        ).map { _ -> AuthResponse(tokens.t1, tokens.t2, user.toDto()) }
    }
}