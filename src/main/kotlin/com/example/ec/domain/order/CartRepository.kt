package com.example.ec.domain.order

import com.example.ec.domain.customer.CustomerId

interface CartRepository {
    fun findByCustomerId(customerId: CustomerId): Cart?
    fun save(cart: Cart)
}
