package com.example.ec.domain.customer

import com.example.ec.domain.exception.DomainValidationException

data class Customer(
    val id: CustomerId,
    val name: CustomerName,
    val email: Email,
    val passwordHash: String,
    val status: CustomerStatus,
    val address: Address?,
) {
    fun updateAddress(newAddress: Address): Customer = copy(address = newAddress)

    companion object {
        fun create(name: String, email: Email, passwordHash: String): Customer =
            Customer(
                id = CustomerId.generate(),
                name = CustomerName(name),
                email = email,
                passwordHash = passwordHash,
                status = CustomerStatus.ACTIVE,
                address = null,
            )

        fun validateRawPassword(password: String) {
            if (password.length < MIN_PASSWORD_LENGTH) {
                throw DomainValidationException("パスワードは${MIN_PASSWORD_LENGTH}文字以上で設定してください")
            }
            if (!password.any { it.isLetter() }) {
                throw DomainValidationException("パスワードには英字を含めてください")
            }
            if (!password.any { it.isDigit() }) {
                throw DomainValidationException("パスワードには数字を含めてください")
            }
        }

        private const val MIN_PASSWORD_LENGTH = 8
    }
}
