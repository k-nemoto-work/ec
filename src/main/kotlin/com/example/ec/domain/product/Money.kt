package com.example.ec.domain.product

import com.example.ec.domain.exception.DomainValidationException

data class Money(val amount: Long) {
    init {
        if (amount <= 0) {
            throw DomainValidationException("価格は1円以上で設定してください")
        }
    }

    operator fun plus(other: Money): Money = Money(amount + other.amount)
}
