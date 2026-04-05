package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException

data class CustomerName(val value: String) {
    companion object {
        const val MAX_LENGTH = 50
    }

    init {
        if (value.isBlank()) {
            throw DomainValidationException("顧客名は空にできません")
        }
        if (value.length > MAX_LENGTH) {
            throw DomainValidationException("顧客名は${MAX_LENGTH}文字以内で入力してください")
        }
    }
}
