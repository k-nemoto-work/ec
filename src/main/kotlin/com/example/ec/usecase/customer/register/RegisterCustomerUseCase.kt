package com.example.ec.usecase.customer.register

import com.example.ec.domain.customer.Customer
import com.example.ec.domain.customer.CustomerRepository
import com.example.ec.domain.customer.Email
import com.example.ec.domain.exception.BusinessRuleViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class RegisterCustomerUseCase(
    private val customerRepository: CustomerRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun execute(command: RegisterCustomerCommand): UUID {
        val email = Email(command.email)

        Customer.validateRawPassword(command.password)

        val existing = customerRepository.findByEmail(email)
        if (existing != null) {
            throw BusinessRuleViolationException("このメールアドレスは既に登録されています: ${command.email}")
        }

        val passwordHash = passwordEncoder.encode(command.password)
        val customer = Customer.create(
            name = command.name,
            email = email,
            passwordHash = passwordHash,
        )

        customerRepository.save(customer)

        return customer.id.value
    }
}
