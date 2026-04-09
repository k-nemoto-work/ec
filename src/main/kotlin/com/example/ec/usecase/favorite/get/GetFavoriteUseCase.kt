package com.example.ec.usecase.favorite.get

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.FavoriteRepository
import com.example.ec.domain.product.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository,
    private val productRepository: ProductRepository,
) {

    fun execute(customerId: UUID): FavoriteResult {
        val favorite = favoriteRepository.findByCustomerId(CustomerId(customerId))
            ?: return FavoriteResult(favoriteId = null, items = emptyList())

        val productIds = favorite.items.map { it.productId }
        val products = productRepository.findAllByIds(productIds).associateBy { it.id }

        val itemResults = favorite.items.mapNotNull { item ->
            val product = products[item.productId] ?: return@mapNotNull null
            FavoriteItemResult(
                productId = item.productId.value,
                productName = product.name.value,
                price = product.price.amount,
                status = product.status.name,
                addedAt = item.addedAt.toString(),
            )
        }

        return FavoriteResult(
            favoriteId = favorite.id.value,
            items = itemResults,
        )
    }
}
