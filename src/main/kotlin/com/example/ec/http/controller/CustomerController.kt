package com.example.ec.http.controller

import com.example.ec.usecase.customer.get_profile.CustomerProfileResult
import com.example.ec.usecase.customer.get_profile.GetCustomerProfileUseCase
import com.example.ec.usecase.customer.update_address.UpdateAddressCommand
import com.example.ec.usecase.customer.update_address.UpdateAddressUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/customers")
class CustomerController(
    private val getCustomerProfileUseCase: GetCustomerProfileUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
) {

    data class AddressRequest(
        val postalCode: String,
        val prefecture: String,
        val city: String,
        val streetAddress: String,
    )

    @GetMapping("/me")
    fun getProfile(authentication: Authentication): ResponseEntity<CustomerProfileResult> {
        val customerId = UUID.fromString(authentication.principal as String)
        val result = getCustomerProfileUseCase.execute(customerId)
        return ResponseEntity.ok(result)
    }

    @PutMapping("/me/address")
    fun updateAddress(
        authentication: Authentication,
        @RequestBody request: AddressRequest,
    ): ResponseEntity<Void> {
        val customerId = UUID.fromString(authentication.principal as String)
        val command = UpdateAddressCommand(
            customerId = customerId,
            postalCode = request.postalCode,
            prefecture = request.prefecture,
            city = request.city,
            streetAddress = request.streetAddress,
        )
        updateAddressUseCase.execute(command)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
