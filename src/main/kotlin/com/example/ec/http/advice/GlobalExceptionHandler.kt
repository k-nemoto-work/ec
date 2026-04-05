package com.example.ec.http.advice

import com.example.ec.domain.exception.AuthenticationException
import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.DomainValidationException
import com.example.ec.domain.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainValidationException::class)
    fun handleDomainValidation(e: DomainValidationException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "VALIDATION_ERROR", message = e.message ?: "バリデーションエラー"))

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(e: AuthenticationException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(code = "AUTHENTICATION_ERROR", message = e.message ?: "認証エラー"))

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolation(e: BusinessRuleViolationException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(code = "BUSINESS_RULE_VIOLATION", message = e.message ?: "ビジネスルール違反"))

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(e: ResourceNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "RESOURCE_NOT_FOUND", message = e.message ?: "リソースが見つかりません"))
}
