package com.example.ec.domain.customer

interface FavoriteRepository {
    fun findByCustomerId(customerId: CustomerId): Favorite?
    fun save(favorite: Favorite)
    fun update(favorite: Favorite)
}
