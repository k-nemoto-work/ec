package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException

data class Address(
    val postalCode: String,
    val prefecture: String,
    val city: String,
    val streetAddress: String,
) {
    init {
        if (postalCode.isBlank()) throw DomainValidationException("郵便番号は空にできません")
        if (prefecture.isBlank()) throw DomainValidationException("都道府県は空にできません")
        if (city.isBlank()) throw DomainValidationException("市区町村は空にできません")
        if (streetAddress.isBlank()) throw DomainValidationException("番地は空にできません")
    }
}
