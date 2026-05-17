package com.ddd.product.domain.model;

import com.ddd.common.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldReserveStockWhenSufficient() {
        Product product = new Product("商品A", new BigDecimal("100.00"), 10);
        product.reserveStock(3);
        assertEquals(3, product.getStock().reservedQuantity());
        assertEquals(10, product.getStock().availableQuantity());
    }

    @Test
    void shouldNotReserveMoreThanAvailable() {
        Product product = new Product("商品A", new BigDecimal("100.00"), 5);
        assertThrows(DomainException.class, () -> product.reserveStock(10));
    }

    @Test
    void shouldReleaseStock() {
        Product product = new Product("商品A", new BigDecimal("100.00"), 10);
        product.reserveStock(5);
        product.releaseStock(3);
        assertEquals(2, product.getStock().reservedQuantity());
    }

    @Test
    void shouldNotReleaseMoreThanReserved() {
        Product product = new Product("商品A", new BigDecimal("100.00"), 10);
        product.reserveStock(5);
        assertThrows(DomainException.class, () -> product.releaseStock(10));
    }

    @Test
    void shouldDeactivateProduct() {
        Product product = new Product("商品A", new BigDecimal("100.00"), 10);
        product.deactivate();
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }

    @Test
    void shouldThrowWhenReservingForInactiveProduct() {
        Product product = new Product("商品A", new BigDecimal("100.00"), 10);
        product.deactivate();
        assertThrows(DomainException.class, () -> product.reserveStock(1));
    }
}
