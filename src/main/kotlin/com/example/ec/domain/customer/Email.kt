package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException

data class Email(val value: String) {
    init {
        if (value.isBlank()) {
            throw DomainValidationException("メールアドレスは空にできません")
        }
        if (value.length > 255) {
            throw DomainValidationException("メールアドレスは255文字以内で入力してください")
        }
        val atIndex = value.indexOf('@')
        if (atIndex <= 0 || atIndex == value.length - 1) {
            throw DomainValidationException("メールアドレスの形式が不正です: $value")
        }
        val domain = value.substring(atIndex + 1)
        if (!domain.contains('.') || domain.startsWith('.') || domain.endsWith('.')) {
            throw DomainValidationException("メールアドレスの形式が不正です: $value")
        }
    }
}
