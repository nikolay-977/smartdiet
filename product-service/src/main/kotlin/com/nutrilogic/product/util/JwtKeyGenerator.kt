package com.nutrilogic.product.util

import io.jsonwebtoken.security.Keys
import java.util.Base64

object JwtKeyGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)
        val base64Key = Base64.getEncoder().encodeToString(key.encoded)

        println("Generated JWT Secret Key:")
        println(base64Key)
        println("Key length: ${base64Key.length * 8} bits")
    }
}