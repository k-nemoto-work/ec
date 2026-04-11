package com.example.ec.usecase.cart.get

import com.example.ec.domain.customer.CustomerId
import com.example.ec.domain.order.Cart
import com.example.ec.domain.order.CartId
import com.example.ec.domain.order.CartItem
import com.example.ec.domain.order.CartRepository
import com.example.ec.domain.product.*
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCartUseCaseTest {

    private val cartRepository: CartRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private lateinit var useCase: GetCartUseCase

    @BeforeEach
    fun setUp() {
        useCase = GetCartUseCase(cartRepository, productRepository)
    }

    @Test
    fun `カートが存在する場合、商品情報と合計金額付きで返す`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()

        val cart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(CartItem(productId = ProductId(productId), addedAt = Clock.System.now())),
        )
        val product = Product(
            id = ProductId(productId),
            name = ProductName("テスト商品"),
            price = Money(3000),
            description = "説明",
            categoryId = CategoryId(categoryId),
            status = ProductStatus.ON_SALE,
        )

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart
        every { productRepository.findAllByIds(listOf(ProductId(productId))) } returns listOf(product)

        // When
        val result = useCase.execute(customerId)

        // Then
        assertEquals(1, result.items.size)
        assertEquals(productId, result.items[0].productId)
        assertEquals("テスト商品", result.items[0].productName)
        assertEquals(3000L, result.items[0].price)
        assertEquals(3000L, result.totalAmount)
    }

    @Test
    fun `カートが存在しない場合、空のカートを返す`() {
        // Given
        val customerId = UUID.randomUUID()
        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns null

        // When
        val result = useCase.execute(customerId)

        // Then
        assertTrue(result.items.isEmpty())
        assertEquals(0L, result.totalAmount)
    }

    @Test
    fun `複数商品の合計金額が正しく計算される`() {
        // Given
        val customerId = UUID.randomUUID()
        val productId1 = UUID.randomUUID()
        val productId2 = UUID.randomUUID()
        val categoryId = UUID.randomUUID()

        val cart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(
                CartItem(productId = ProductId(productId1), addedAt = Clock.System.now()),
                CartItem(productId = ProductId(productId2), addedAt = Clock.System.now()),
            ),
        )
        val products = listOf(
            Product(
                id = ProductId(productId1),
                name = ProductName("商品1"),
                price = Money(1000),
                description = "説明",
                categoryId = CategoryId(categoryId),
                status = ProductStatus.ON_SALE,
            ),
            Product(
                id = ProductId(productId2),
                name = ProductName("商品2"),
                price = Money(2500),
                description = "説明",
                categoryId = CategoryId(categoryId),
                status = ProductStatus.ON_SALE,
            ),
        )

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart
        every { productRepository.findAllByIds(any()) } returns products

        // When
        val result = useCase.execute(customerId)

        // Then
        assertEquals(2, result.items.size)
        assertEquals(3500L, result.totalAmount)
    }

    @Test
    fun `カタログから削除された商品はレスポンスに含まれない`() {
        // Given
        val customerId = UUID.randomUUID()
        val existingProductId = UUID.randomUUID()
        val deletedProductId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()

        val cart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(
                CartItem(productId = ProductId(existingProductId), addedAt = Clock.System.now()),
                CartItem(productId = ProductId(deletedProductId), addedAt = Clock.System.now()),
            ),
        )
        val existingProduct = Product(
            id = ProductId(existingProductId),
            name = ProductName("存在する商品"),
            price = Money(1000),
            description = "説明",
            categoryId = CategoryId(categoryId),
            status = ProductStatus.ON_SALE,
        )

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart
        every { productRepository.findAllByIds(any()) } returns listOf(existingProduct)
        justRun { cartRepository.save(any()) }

        // When
        val result = useCase.execute(customerId)

        // Then
        assertEquals(1, result.items.size)
        assertEquals(existingProductId, result.items[0].productId)
        assertEquals(1000L, result.totalAmount)
    }

    @Test
    fun `カタログから削除された商品はDBのカートからも除去される`() {
        // Given
        val customerId = UUID.randomUUID()
        val existingProductId = UUID.randomUUID()
        val deletedProductId = UUID.randomUUID()
        val categoryId = UUID.randomUUID()

        val cart = Cart(
            id = CartId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            items = listOf(
                CartItem(productId = ProductId(existingProductId), addedAt = Clock.System.now()),
                CartItem(productId = ProductId(deletedProductId), addedAt = Clock.System.now()),
            ),
        )
        val existingProduct = Product(
            id = ProductId(existingProductId),
            name = ProductName("存在する商品"),
            price = Money(1000),
            description = "説明",
            categoryId = CategoryId(categoryId),
            status = ProductStatus.ON_SALE,
        )

        every { cartRepository.findByCustomerId(CustomerId(customerId)) } returns cart
        every { productRepository.findAllByIds(any()) } returns listOf(existingProduct)
        justRun { cartRepository.save(any()) }

        // When
        useCase.execute(customerId)

        // Then: 削除済み商品を除いたカートで save が呼ばれること
        verify(exactly = 1) {
            cartRepository.save(
                match { savedCart ->
                    savedCart.items.size == 1 &&
                        savedCart.items[0].productId == ProductId(existingProductId)
                }
            )
        }
    }
}
