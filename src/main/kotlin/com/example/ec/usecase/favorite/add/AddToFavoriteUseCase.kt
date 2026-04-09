package com.example.ec.usecase.favorite.add

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.Favorite
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.exception.ResourceNotFoundException
import com.example.ec.domain.product.ProductId
import com.example.ec.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AddToFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository,
    private val productRepository: ProductRepository,
) {

    fun execute(command: AddToFavoriteCommand) {
        val customerId = CustomerId(command.customerId)
        val productId = ProductId(command.productId)

        val product = productRepository.findById(productId)
            ?: throw ResourceNotFoundException("商品", command.productId.toString())

        val favorite = favoriteRepository.findByCustomerId(customerId) ?: Favorite.create(customerId)
        val updatedFavorite = favorite.addItem(productId, product.status)
        favoriteRepository.save(updatedFavorite)
    }
}
