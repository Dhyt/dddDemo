package com.ddd.order.domain.model;

import com.ddd.common.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithAmountAndCurrency() {
        Money money = new Money(new BigDecimal("100.00"), "CNY");
        assertEquals(0, new BigDecimal("100.00").compareTo(money.amount()));
        assertEquals("CNY", money.currency());
    }

    @Test
    void shouldBeImmutable() {
        Money original = new Money(new BigDecimal("100.00"), "CNY");
        original.add(new Money(new BigDecimal("50.00"), "CNY"));
        assertEquals(0, new BigDecimal("100.00").compareTo(original.amount()));
    }

    @Test
    void shouldAddSameCurrency() {
        Money a = new Money(new BigDecimal("100.00"), "CNY");
        Money b = new Money(new BigDecimal("50.00"), "CNY");
        Money result = a.add(b);
        assertEquals(0, new BigDecimal("150.00").compareTo(result.amount()));
    }

    @Test
    void shouldThrowWhenAddingDifferentCurrencies() {
        Money a = new Money(new BigDecimal("100.00"), "CNY");
        Money b = new Money(new BigDecimal("50.00"), "USD");
        assertThrows(DomainException.class, () -> a.add(b));
    }

    @Test
    void shouldImplementValueEquality() {
        Money a = new Money(new BigDecimal("100.00"), "CNY");
        Money b = new Money(new BigDecimal("100.00"), "CNY");
        Money c = new Money(new BigDecimal("100.00"), "USD");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
