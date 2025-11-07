package com.medipharm.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import reactor.core.publisher.Mono
import java.time.Duration

@Configuration
class GracefulShutdownConfig {
    @EventListener(ContextClosedEvent::class)
    fun onShutdown() {
        //Wait for in-flight requests to complete
        Mono.delay(Duration.ofSeconds(5)).block()
    }
}