package com.example.ec.domain.product

import com.example.ec.domain.exception.BusinessRuleViolationException
import com.example.ec.domain.exception.DomainValidationException

data class Product(
    val id: ProductId,
    val name: ProductName,
    val price: Money,
    val description: String,
    val categoryId: CategoryId,
    val status: ProductStatus,
) {
    /**
     * 商品情報を更新する。
     * 販売中または非公開の商品のみ更新できる。
     */
    fun update(name: ProductName, price: Money, description: String, categoryId: CategoryId): Product {
        if (status == ProductStatus.RESERVED || status == ProductStatus.SOLD) {
            throw BusinessRuleViolationException("売約済みまたは売却済みの商品は更新できません")
        }
        validateDescription(description)
        return copy(name = name, price = price, description = description, categoryId = categoryId)
    }

    /**
     * 商品ステータスを変更する。
     * 許可される遷移:
     * - PRIVATE → ON_SALE（公開）
     * - ON_SALE → PRIVATE（非公開化）
     * - ON_SALE → RESERVED（注文確定による自動遷移）
     * - RESERVED → SOLD（配送完了による自動遷移）
     * - RESERVED → ON_SALE（注文キャンセルによる復元）
     * - SOLD → *: 変更不可
     */
    fun changeStatus(newStatus: ProductStatus): Product {
        val allowed = when (status) {
            ProductStatus.PRIVATE -> newStatus == ProductStatus.ON_SALE
            ProductStatus.ON_SALE -> newStatus == ProductStatus.PRIVATE || newStatus == ProductStatus.RESERVED
            ProductStatus.RESERVED -> newStatus == ProductStatus.SOLD || newStatus == ProductStatus.ON_SALE
            ProductStatus.SOLD -> false
        }
        if (!allowed) {
            throw BusinessRuleViolationException("${status} から ${newStatus} へのステータス変更は許可されていません")
        }
        return copy(status = newStatus)
    }

    companion object {
        private const val MAX_DESCRIPTION_LENGTH = 2000

        fun create(name: ProductName, price: Money, description: String, categoryId: CategoryId): Product {
            validateDescription(description)
            return Product(
                id = ProductId.generate(),
                name = name,
                price = price,
                description = description,
                categoryId = categoryId,
                status = ProductStatus.PRIVATE,
            )
        }

        fun validateDescription(description: String) {
            if (description.length > MAX_DESCRIPTION_LENGTH) {
                throw DomainValidationException("説明は${MAX_DESCRIPTION_LENGTH}文字以内で設定してください")
            }
        }
    }
}
