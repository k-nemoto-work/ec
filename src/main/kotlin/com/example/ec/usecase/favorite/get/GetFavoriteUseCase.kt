package com.example.ec.usecase.favorite.get

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetFavoriteUseCase(
    private val favoriteQueryService: FavoriteQueryService,
) {

    fun execute(customerId: UUID): FavoriteResult =
        favoriteQueryService.findByCustomerId(customerId)
}
