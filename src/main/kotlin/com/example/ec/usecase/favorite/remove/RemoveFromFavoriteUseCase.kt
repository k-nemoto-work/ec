package com.example.ec.usecase.favorite.remove

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.ProductId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RemoveFromFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository,
) {

    fun execute(command: RemoveFromFavoriteCommand) {
        val customerId = CustomerId(command.customerId)
        val productId = ProductId(command.productId)

        val favorite = favoriteRepository.findByCustomerId(customerId)
            ?: throw ResourceNotFoundException("お気に入り", customerId.value.toString())

        val updatedFavorite = favorite.removeItem(productId)
        favoriteRepository.update(updatedFavorite)
    }
}
