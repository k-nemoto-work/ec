package com.example.ec.usecase.customer.login

import com.example.ec.domain.customer.CustomerRepository
import com.example.ec.domain.customer.CustomerStatus
import com.example.ec.domain.customer.Email
import com.example.ec.domain.exception.AuthenticationException
import com.example.ec.infrastructure.config.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LoginUseCase(
    private val customerRepository: CustomerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    fun execute(command: LoginCommand): LoginResult {
        val email = Email(command.email)

        val customer = customerRepository.findByEmail(email)
            ?: throw AuthenticationException("メールアドレスまたはパスワードが正しくありません")

        if (customer.status != CustomerStatus.ACTIVE) {
            throw AuthenticationException("このアカウントは無効化されています")
        }

        if (!passwordEncoder.matches(command.password, customer.passwordHash)) {
            throw AuthenticationException("メールアドレスまたはパスワードが正しくありません")
        }

        val token = jwtTokenProvider.generateToken(customer.id)

        return LoginResult(
            accessToken = token,
            expiresIn = jwtTokenProvider.getExpirationSeconds(),
        )
    }
}
