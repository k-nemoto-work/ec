package com.example.ec.domain.product

import com.example.ec.domain.exception.DomainValidationException

data class ProductName(val value: String) {
    companion object {
        const val MAX_LENGTH = 100
    }

    init {
        if (value.isBlank()) {
            throw DomainValidationException("商品名は空にできません")
        }
        if (value.length > MAX_LENGTH) {
            throw DomainValidationException("商品名は${MAX_LENGTH}文字以内で設定してください")
        }
    }
}
