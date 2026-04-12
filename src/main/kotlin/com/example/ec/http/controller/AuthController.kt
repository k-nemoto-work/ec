package com.example.ec.http.controller

import com.example.ec.usecase.customer.login.LoginCommand
import com.example.ec.usecase.customer.login.LoginUseCase
import com.example.ec.usecase.customer.register.RegisterCustomerCommand
import com.example.ec.usecase.customer.register.RegisterCustomerUseCase
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
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
    private val environment: Environment,
) {

    data class RegisterRequest(val name: String, val email: String, val password: String)
    data class RegisterResponse(val customerId: String)

    data class LoginRequest(val email: String, val password: String)

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
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val command = LoginCommand(
            email = request.email,
            password = request.password,
        )
        val result = loginUseCase.execute(command)
        val secure = !environment.acceptsProfiles(Profiles.of("dev"))

        response.addCookie(
            Cookie("jwt", result.accessToken).apply {
                isHttpOnly = true
                this.secure = secure
                path = "/"
                maxAge = result.expiresIn.toInt()
                setAttribute("SameSite", "Strict")
            }
        )

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Void> {
        val secure = !environment.acceptsProfiles(Profiles.of("dev"))

        response.addCookie(
            Cookie("jwt", "").apply {
                isHttpOnly = true
                this.secure = secure
                path = "/"
                maxAge = 0
                setAttribute("SameSite", "Strict")
            }
        )

        return ResponseEntity.noContent().build()
    }
}
