package com.example.ec.http.controller

import com.example.ec.usecase.customer.login.LoginCommand
import com.example.ec.usecase.customer.login.LoginUseCase
import com.example.ec.usecase.customer.register.RegisterCustomerCommand
import com.example.ec.usecase.customer.register.RegisterCustomerUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerCustomerUseCase: RegisterCustomerUseCase,
    private val loginUseCase: LoginUseCase,
) {

    data class RegisterRequest(val name: String, val email: String, val password: String)
    data class RegisterResponse(val customerId: String)

    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(val accessToken: String, val expiresIn: Long)

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        val command = RegisterCustomerCommand(
            name = request.name,
            email = request.email,
            password = request.password,
        )
        val customerId = registerCustomerUseCase.execute(command)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RegisterResponse(customerId = customerId.toString()))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val command = LoginCommand(
            email = request.email,
            password = request.password,
        )
        val result = loginUseCase.execute(command)
        return ResponseEntity.ok(
            LoginResponse(
                accessToken = result.accessToken,
                expiresIn = result.expiresIn,
            )
        )
    }
}
