package com.example.ec.http.controller

import com.example.ec.usecase.cart.add.AddToCartCommand
import com.example.ec.usecase.cart.add.AddToCartUseCase
import com.example.ec.usecase.cart.get.CartResult
import com.example.ec.usecase.cart.get.GetCartUseCase
import com.example.ec.usecase.cart.remove.RemoveFromCartCommand
import com.example.ec.usecase.cart.remove.RemoveFromCartUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cart")
class CartController(
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
) {

    data class AddToCartRequest(val productId: UUID)

    @GetMapping
    fun getCart(authentication: Authentication): ResponseEntity<CartResult> {
        val customerId = UUID.fromString(authentication.principal as String)
        val result = getCartUseCase.execute(customerId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/items")
    fun addItem(
        authentication: Authentication,
        @RequestBody request: AddToCartRequest,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        addToCartUseCase.execute(
            AddToCartCommand(customerId = customerId, productId = request.productId)
        )
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @DeleteMapping("/items/{productId}")
    fun removeItem(
        authentication: Authentication,
        @PathVariable productId: UUID,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        removeFromCartUseCase.execute(
            RemoveFromCartCommand(customerId = customerId, productId = productId)
        )
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
