package com.example.ec.usecase.customer.get_profile

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.customer.CustomerRepository
import com.example.ec.domain.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetCustomerProfileUseCase(
    private val customerRepository: CustomerRepository,
) {

    fun execute(customerId: UUID): CustomerProfileResult {
        val customer = customerRepository.findById(CustomerId(customerId))
            ?: throw ResourceNotFoundException("Customer", customerId.toString())

        return CustomerProfileResult(
            id = customer.id.value,
            name = customer.name.value,
            email = customer.email.value,
            status = customer.status.name,
            address = customer.address?.let {
                AddressResult(
                    postalCode = it.postalCode,
                    prefecture = it.prefecture,
                    city = it.city,
                    streetAddress = it.streetAddress,
                )
            },
        )
    }
}
