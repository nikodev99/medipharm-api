package com.medipharm.backend.helper

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest

data class ClientInfo(
    val userAgent: String? = null,
    val clientName: String? = null,
    val clientType: String? = null,
    val clientIp: String? = null,
    val deviceFingerprint: String? = null
)

object RequestUtils {
    fun fromRequest(request: ServerHttpRequest): ClientInfo {
        val headers = request.headers

        val userAgent = headers.getFirst(HttpHeaders.USER_AGENT)
        val forwardFor = headers.getFirst("X-Forwarded-For")
        val mobile = headers.getFirst("Sec-Ch-Ua-Mobile")
        val platform = headers.getFirst("Sec-Ch-Ua-Platform")
        val clientType = headers.getFirst("Sec-Ch-Ua")

        val device = if (mobile != null && mobile.startsWith("?")) {
            mobile
        }else {
            platform
        }

        return ClientInfo(
            userAgent = userAgent,
            clientName = forwardFor,
            clientType = clientType,
            clientIp = forwardFor?.split(",")?.firstOrNull()?.trim() ?: request.remoteAddress?.address?.hostAddress,
            deviceFingerprint = device
        )
    }
}