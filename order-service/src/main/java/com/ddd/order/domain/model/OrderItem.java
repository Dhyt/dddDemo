package com.ddd.order.domain.model;

/**
 * 订单项实体 — 记录下单时的商品快照。
 *
 * DDD: Entity — 有唯一标识（orderItemId），可变，通过 ID 判断相等性。
 *      使用商品快照而非引用 Product 对象，确保订单历史不受商品信息变更影响。
 */
public class OrderItem {
    private Long orderItemId;
    private final ProductId productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;
    private Money subtotal;

    public OrderItem(ProductId productId, String productName, int quantity, Money unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("数量必须大于0");
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(quantity);
    }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public ProductId getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
    public Money getSubtotal() { return subtotal; }
}
