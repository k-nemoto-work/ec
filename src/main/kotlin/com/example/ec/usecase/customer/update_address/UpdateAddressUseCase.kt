package com.example.ec.usecase.customer.update_address

import com.example.ec.domain.customer.Address
import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.CustomerRepository
import com.example.ec.domain.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateAddressUseCase(
    private val customerRepository: CustomerRepository,
) {

    fun execute(command: UpdateAddressCommand) {
        val customerId = CustomerId(command.customerId)

        val customer = customerRepository.findById(customerId)
            ?: throw ResourceNotFoundException("Customer", command.customerId.toString())

        val address = Address(
            postalCode = command.postalCode,
            prefecture = command.prefecture,
            city = command.city,
            streetAddress = command.streetAddress,
        )

        val updatedCustomer = customer.updateAddress(address)
        customerRepository.update(updatedCustomer)
    }
}
