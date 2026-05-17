package com.ddd.product.domain.model;

import com.ddd.common.domain.AggregateRoot;
import com.ddd.common.exception.DomainException;
import com.ddd.product.domain.event.StockChangedEvent;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 商品聚合根 — 商品信息与库存管理。
 *
 * DDD: Aggregate Root — 库存操作必须通过聚合根方法，确保业务规则不变。
 *
 * 业务规则:
 * 1. 预留库存不能超过可用库存
 * 2. 下架商品不可预留库存
 * 3. 库存操作通过 reserveStock / releaseStock 方法
 */
public class Product extends AggregateRoot<ProductId> {

    private String name;
    private String description;
    private BigDecimal price;
    private Stock stock;
    private ProductStatus status;
    private Instant createdAt;

    public Product(String name, BigDecimal price, int availableQuantity) {
        this.name = name;
        this.price = price;
        this.stock = new Stock(availableQuantity, 0);
        this.status = ProductStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public StockChangedEvent reserveStock(int quantity) {
        if (this.status != ProductStatus.ACTIVE) {
            throw new DomainException("PRODUCT_INACTIVE", "商品已下架，不可操作库存");
        }
        if (stock.availableQuantity() - stock.reservedQuantity() < quantity) {
            throw new DomainException("INSUFFICIENT_STOCK",
                    "库存不足: 可用 " + (stock.availableQuantity() - stock.reservedQuantity()) + ", 需要 " + quantity);
        }
        this.stock = stock.reserve(quantity);
        if (stock.availableQuantity() - stock.reservedQuantity() == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
        return new StockChangedEvent(getId(), quantity, stock);
    }

    public StockChangedEvent releaseStock(int quantity) {
        try {
            this.stock = stock.release(quantity);
        } catch (IllegalArgumentException e) {
            throw new DomainException("INVALID_RELEASE", e.getMessage());
        }
        if (this.status == ProductStatus.OUT_OF_STOCK && stock.availableQuantity() > stock.reservedQuantity()) {
            this.status = ProductStatus.ACTIVE;
        }
        return new StockChangedEvent(getId(), -quantity, stock);
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Stock getStock() { return stock; }
    public ProductStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { super.setId(new ProductId(id)); }
    public void setDescription(String description) { this.description = description; }
}
