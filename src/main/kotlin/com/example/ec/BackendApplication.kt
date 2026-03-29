package com.example.ec

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class EcApplication

@RestController
class HealthController {
    
    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf(
        "status" to "OK", 
        "message" to "EC Backend Ready!"
    )
    
    @GetMapping("/db-test")
    fun dbTest(): Map<String, String> {
        val result = transaction {
            exec("SELECT 1 AS val") { rs ->
                rs.next()
                rs.getInt("val")
            }
        }
        return mapOf("status" to "OK", "result" to result.toString())
    }
}

fun main(args: Array<String>) {
    runApplication<EcApplication>(*args)
}
