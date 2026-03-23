package com.nutrilogic.recommendation.config

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Configuration
class FeignConfig {

    @Bean
    fun authorizationHeaderInterceptor(): RequestInterceptor {
        return RequestInterceptor { requestTemplate ->
            val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            val request = attributes?.request
            val authHeader = request?.getHeader("Authorization")
            if (!authHeader.isNullOrBlank()) {
                requestTemplate.header("Authorization", authHeader)
            }
        }
    }
}