package com.example.ec.http.controller

import com.example.ec.usecase.product.get.GetProductForManagementUseCase
import com.example.ec.usecase.product.get_categories.GetCategoriesUseCase
import com.example.ec.usecase.product.get.GetProductUseCase
import com.example.ec.usecase.product.get.ProductResult
import com.example.ec.usecase.product.list.ListProductsQuery
import com.example.ec.usecase.product.list.ListProductsUseCase
import com.example.ec.usecase.product.list.ProductListResult
import com.example.ec.usecase.product.register.RegisterProductCommand
import com.example.ec.usecase.product.register.RegisterProductUseCase
import com.example.ec.usecase.product.update.UpdateProductCommand
import com.example.ec.usecase.product.update.UpdateProductUseCase
import com.example.ec.usecase.product.update_status.UpdateProductStatusCommand
import com.example.ec.usecase.product.update_status.UpdateProductStatusUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val listProductsUseCase: ListProductsUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val getProductForManagementUseCase: GetProductForManagementUseCase,
    private val registerProductUseCase: RegisterProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val updateProductStatusUseCase: UpdateProductStatusUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) {

    data class CategoryResponse(val id: UUID, val name: String)

    data class RegisterProductRequest(
        val name: String,
        val price: Long,
        val description: String,
        val categoryId: UUID,
    )

    data class RegisterProductResponse(val productId: String)

    data class UpdateProductRequest(
        val name: String,
        val price: Long,
        val description: String,
        val categoryId: UUID,
    )

    data class UpdateProductStatusRequest(val status: String)

    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = getCategoriesUseCase.execute()
        return ResponseEntity.ok(categories.map { CategoryResponse(it.id, it.name) })
    }

    @GetMapping
    fun listProducts(
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ProductListResult> {
        val result = listProductsUseCase.execute(
            ListProductsQuery(categoryId = categoryId, page = page, size = size)
        )
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: UUID): ResponseEntity<ProductResult> {
        val result = getProductUseCase.execute(productId)
        return ResponseEntity.ok(result)
    }

    @PostMapping
    fun registerProduct(@RequestBody request: RegisterProductRequest): ResponseEntity<RegisterProductResponse> {
        val command = RegisterProductCommand(
            name = request.name,
            price = request.price,
            description = request.description,
            categoryId = request.categoryId,
        )
        val productId = registerProductUseCase.execute(command)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RegisterProductResponse(productId = productId.toString()))
    }

    @GetMapping("/{productId}/management")
    fun getProductForManagement(@PathVariable productId: UUID): ResponseEntity<ProductResult> {
        val result = getProductForManagementUseCase.execute(productId)
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable productId: UUID,
        @RequestBody request: UpdateProductRequest,
    ): ResponseEntity<ProductResult> {
        val command = UpdateProductCommand(
            productId = productId,
            name = request.name,
            price = request.price,
            description = request.description,
            categoryId = request.categoryId,
        )
        val result = updateProductUseCase.execute(command)
        return ResponseEntity.ok(result)
    }

    @PatchMapping("/{productId}/status")
    fun updateProductStatus(
        @PathVariable productId: UUID,
        @RequestBody request: UpdateProductStatusRequest,
    ): ResponseEntity<ProductResult> {
        val command = UpdateProductStatusCommand(
            productId = productId,
            status = request.status,
        )
        val result = updateProductStatusUseCase.execute(command)
        return ResponseEntity.ok(result)
    }
}
