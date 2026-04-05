package com.example.ec.infrastructure.config

import com.example.ec.domain.customer.CustomerId
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(private val jwtConfig: JwtConfig) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray())
    }

    fun generateToken(customerId: CustomerId): String {
        val now = Date()
        val expiration = Date(now.time + jwtConfig.expiration * 1000)

        return Jwts.builder()
            .subject(customerId.value.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    fun getCustomerIdFromToken(token: String): CustomerId? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            CustomerId(UUID.fromString(claims.subject))
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun getExpirationSeconds(): Long = jwtConfig.expiration
}
