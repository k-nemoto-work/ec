package com.example.ec.http.controller

import com.example.ec.usecase.favorite.add.AddToFavoriteCommand
import com.example.ec.usecase.favorite.add.AddToFavoriteUseCase
import com.example.ec.usecase.favorite.get.FavoriteResult
import com.example.ec.usecase.favorite.get.GetFavoriteUseCase
import com.example.ec.usecase.favorite.remove.RemoveFromFavoriteCommand
import com.example.ec.usecase.favorite.remove.RemoveFromFavoriteUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/favorites")
class FavoriteController(
    private val getFavoriteUseCase: GetFavoriteUseCase,
    private val addToFavoriteUseCase: AddToFavoriteUseCase,
    private val removeFromFavoriteUseCase: RemoveFromFavoriteUseCase,
) {

    data class AddToFavoriteRequest(val productId: UUID)

    @GetMapping
    fun getFavorite(authentication: Authentication): ResponseEntity<FavoriteResult> {
        val customerId = UUID.fromString(authentication.principal as String)
        val result = getFavoriteUseCase.execute(customerId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/items")
    fun addItem(
        authentication: Authentication,
        @RequestBody request: AddToFavoriteRequest,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        addToFavoriteUseCase.execute(
            AddToFavoriteCommand(customerId = customerId, productId = request.productId)
        )
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @DeleteMapping("/items/{productId}")
    fun removeItem(
        authentication: Authentication,
        @PathVariable productId: UUID,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        removeFromFavoriteUseCase.execute(
            RemoveFromFavoriteCommand(customerId = customerId, productId = productId)
        )
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
