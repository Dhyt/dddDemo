# DDD 订单管理系统 Demo 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个完整的 DDD 订单管理系统 Demo，覆盖战略设计与战术设计核心概念，包含详细的中文教学注释。

**Architecture:** 3 个微服务（Order/Product/Payment）+ Eureka + Gateway + RabbitMQ + MySQL，每个服务按六边形架构分为 Domain/Application/Infrastructure/Interface 四层。

**Tech Stack:** Java 17, Spring Boot 3.2.5, Spring Cloud 2023.0.1, Spring Data JPA, MySQL 8.0, RabbitMQ, Maven 多模块

---

### Task 1: 父 POM、Common 模块与项目骨架

**Files:**
- Create: `pom.xml`
- Create: `.gitignore`
- Create: `common/pom.xml`
- Create: `common/src/main/java/com/ddd/common/domain/AggregateRoot.java`
- Create: `common/src/main/java/com/ddd/common/domain/DomainEvent.java`
- Create: `common/src/main/java/com/ddd/common/domain/ValueObject.java`
- Create: `common/src/main/java/com/ddd/common/exception/DomainException.java`
- Create: 所有子模块的空 pom.xml（order-service, product-service, payment-service, discovery-service, gateway-service）

- [ ] **Step 1: 创建父 POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ddd</groupId>
    <artifactId>ddd-demo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>DDD Demo - Order Management System</name>

    <modules>
        <module>common</module>
        <module>order-service</module>
        <module>product-service</module>
        <module>payment-service</module>
        <module>discovery-service</module>
        <module>gateway-service</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <spring-boot.version>3.2.5</spring-boot.version>
        <spring-cloud.version>2023.0.1</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.ddd</groupId>
                <artifactId>common</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建 .gitignore**

```gitignore
target/
*.class
*.jar
*.war
*.log
.idea/
*.iml
.vscode/
.DS_Store
application-local.yml
```

- [ ] **Step 3: 创建 common 模块**

common/pom.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xml/ns/maven-4.0.0.xsd">
    <parent>
        <groupId>com.ddd</groupId>
        <artifactId>ddd-demo</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>common</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <!-- No Spring dependencies - pure Java domain abstractions -->
    </dependencies>
</project>
```

- [ ] **Step 4: 创建 Common 领域抽象类**

```java
// common/src/main/java/com/ddd/common/domain/AggregateRoot.java
package com.ddd.common.domain;

/**
 * 聚合根基类 — 所有聚合根的父类。
 *
 * DDD: 聚合根是聚合的入口，外部只能通过聚合根访问聚合内的实体。
 *      聚合根负责保证其内部所有业务规则（不变量）的一致性。
 */
public abstract class AggregateRoot<T> {
    private T id;

    public T getId() {
        return id;
    }

    protected void setId(T id) {
        this.id = id;
    }
}
```

```java
// common/src/main/java/com/ddd/common/domain/DomainEvent.java
package com.ddd.common.domain;

import java.time.Instant;

/**
 * 领域事件接口 — 所有领域事件的父接口。
 *
 * DDD: 领域事件表示领域中发生的、对业务有意义的过去事件。
 *      命名使用过去时态（如 OrderSubmittedEvent），因为事件已经发生不可更改。
 *      领域事件是实现限界上下文解耦的关键手段。
 */
public interface DomainEvent {
    /** 事件发生的时间戳 */
    Instant occurredAt();
}
```

```java
// common/src/main/java/com/ddd/common/domain/ValueObject.java
package com.ddd.common.domain;

import java.io.Serializable;

/**
 * 值对象基类标记接口。
 *
 * DDD: 值对象通过属性值来定义自身，没有唯一标识，是不可变的。
 *      两个值对象如果所有属性值相等，则视为同一个对象。
 *      equals() 和 hashCode() 必须基于所有属性实现。
 */
public interface ValueObject extends Serializable {
}
```

```java
// common/src/main/java/com/ddd/common/exception/DomainException.java
package com.ddd.common.exception;

/**
 * 领域异常基类 — 所有业务规则违反都抛出此异常或其子类。
 *
 * DDD: 领域异常是 Ubiquitous Language 的一部分，异常信息应使用业务术语描述。
 *      让调用方（Application 层）能够理解业务层面发生了什么问题。
 */
public class DomainException extends RuntimeException {
    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DomainException(String message) {
        this("DOMAIN_ERROR", message);
    }

    public String getCode() {
        return code;
    }
}
```

- [ ] **Step 5: 创建各子模块空 pom.xml**

order-service/pom.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xml/ns/maven-4.0.0.xsd">
    <parent>
        <groupId>com.ddd</groupId>
        <artifactId>ddd-demo</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>order-service</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.ddd</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

相同的结构创建 product-service/pom.xml、payment-service/pom.xml（同样依赖 common、web、jpa、eureka-client、amqp、mysql-connector-j）。

discovery-service/pom.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xml/ns/maven-4.0.0.xsd">
    <parent>
        <groupId>com.ddd</groupId>
        <artifactId>ddd-demo</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>discovery-service</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>
</project>
```

gateway-service/pom.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xml/ns/maven-4.0.0.xsd">
    <parent>
        <groupId>com.ddd</groupId>
        <artifactId>ddd-demo</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>gateway-service</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: 验证 Maven 编译**

```bash
cd /Users/hhh/project/dddDemo
mvn clean compile -q
```
Expected: BUILD SUCCESS（子模块可能缺少 Spring Boot 启动类导致警告，忽略即可）

- [ ] **Step 7: 提交**

```bash
git init
git add .
git commit -m "feat: 初始化 Maven 多模块项目骨架与 Common 领域抽象"
```

---

### Task 2: Order Service — Domain 层

**Files:**
- Create: `order-service/src/main/java/com/ddd/order/domain/model/OrderId.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/model/CustomerId.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/model/Money.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/model/OrderStatus.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/model/Address.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/model/OrderItem.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/model/Order.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/event/OrderSubmittedEvent.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/event/OrderPaidEvent.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/event/OrderCancelledEvent.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/event/OrderDeliveredEvent.java`
- Create: `order-service/src/main/java/com/ddd/order/domain/repository/OrderRepository.java`
- Create: `order-service/src/test/java/com/ddd/order/domain/model/MoneyTest.java`
- Create: `order-service/src/test/java/com/ddd/order/domain/model/OrderTest.java`

- [ ] **Step 1: TDD — 先写 Money 值对象测试**

```java
// order-service/src/test/java/com/ddd/order/domain/model/MoneyTest.java
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
        original.add(new Money(new BigDecimal("50.00"), "CNY")); // 应返回新对象，不修改原对象
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
```

- [ ] **Step 2: 实现 Money 值对象**

```java
// order-service/src/main/java/com/ddd/order/domain/model/Money.java
package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;
import com.ddd.common.exception.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * 金额值对象 — 不可变，包含金额和币种。
 *
 * DDD: Value Object — 通过属性值定义自身，无唯一标识，不可变。
 *      所有金额操作必须通过对象方法完成，确保币种一致性。
 */
public record Money(BigDecimal amount, String currency) implements ValueObject {

    public Money {
        // 防御性拷贝 + 校验
        if (amount == null || currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("金额和币种不能为空");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("CURRENCY_MISMATCH",
                    "不同币种不可相加: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("CURRENCY_MISMATCH",
                    "不同币种不可相减: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("CURRENCY_MISMATCH", "不同币种不可比较");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
```

- [ ] **Step 3: TDD — 写 Order 聚合测试**

```java
// order-service/src/test/java/com/ddd/order/domain/model/OrderTest.java
package com.ddd.order.domain.model;

import com.ddd.common.exception.DomainException;
import com.ddd.order.domain.event.OrderCancelledEvent;
import com.ddd.order.domain.event.OrderDeliveredEvent;
import com.ddd.order.domain.event.OrderPaidEvent;
import com.ddd.order.domain.event.OrderSubmittedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Money price100;
    private Money price200;
    private Address address;

    @BeforeEach
    void setUp() {
        price100 = new Money(new BigDecimal("100.00"), "CNY");
        price200 = new Money(new BigDecimal("200.00"), "CNY");
        address = new Address("北京路1号", "上海", "200000");
    }

    @Test
    void shouldCreateOrderWithItems() {
        Order order = new Order(new CustomerId(1L), address);
        order.addItem(new ProductId(1L), "商品A", 2, price100);
        order.addItem(new ProductId(2L), "商品B", 1, price200);

        assertEquals(2, order.getItems().size());
        assertEquals(0, new BigDecimal("400.00").compareTo(order.getTotalAmount().amount()));
    }

    @Test
    void shouldPublishEventWhenSubmitted() {
        Order order = new Order(new CustomerId(1L), address);
        order.addItem(new ProductId(1L), "商品A", 1, price100);

        OrderSubmittedEvent event = order.submit();
        assertNotNull(event);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldNotSubmitEmptyOrder() {
        Order order = new Order(new CustomerId(1L), address);
        assertThrows(DomainException.class, order::submit);
    }

    @Test
    void shouldCancelPendingOrder() {
        Order order = createPendingOrder();
        OrderCancelledEvent event = order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(event);
    }

    @Test
    void shouldNotCancelPaidOrder() {
        Order order = createPendingOrder();
        order.submit();
        order.markPaid();
        assertThrows(DomainException.class, order::cancel);
    }

    @Test
    void shouldTransitionFromPendingToPaid() {
        Order order = createPendingOrder();
        order.submit();
        OrderPaidEvent event = order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(event);
    }

    @Test
    void shouldTransitionFromPaidToDelivered() {
        Order order = createPendingOrder();
        order.submit();
        order.markPaid();
        OrderDeliveredEvent event = order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(event);
    }

    private Order createPendingOrder() {
        Order order = new Order(new CustomerId(1L), address);
        order.addItem(new ProductId(1L), "商品A", 1, price100);
        order.submit();
        return order;
    }
}
```

- [ ] **Step 4: 实现 Order 领域模型**

```java
// order-service/src/main/java/com/ddd/order/domain/model/OrderId.java
package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;

public record OrderId(Long value) implements ValueObject {
    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("订单ID必须为正数");
        }
    }
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/model/CustomerId.java
package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;

public record CustomerId(Long value) implements ValueObject {
    public CustomerId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("客户ID必须为正数");
        }
    }
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/model/OrderStatus.java
package com.ddd.order.domain.model;

/**
 * 订单状态值对象 — 枚举订单生命周期中的所有状态。
 *
 * DDD: 状态用枚举作为 Value Object，表示订单在其生命周期中可能处于的阶段。
 */
public enum OrderStatus {
    PENDING,      // 待处理
    PAID,         // 已支付
    SHIPPED,      // 已发货
    DELIVERED,    // 已签收
    CANCELLED     // 已取消
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/model/Address.java
package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;

public record Address(String street, String city, String zipCode) implements ValueObject {
    public Address {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("街道地址不能为空");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("城市不能为空");
        }
    }
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/model/ProductId.java
package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;

public record ProductId(Long value) implements ValueObject {
    public ProductId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("商品ID必须为正数");
        }
    }
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/model/OrderItem.java
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
```

```java
// order-service/src/main/java/com/ddd/order/domain/model/Order.java
package com.ddd.order.domain.model;

import com.ddd.common.domain.AggregateRoot;
import com.ddd.common.exception.DomainException;
import com.ddd.order.domain.event.OrderCancelledEvent;
import com.ddd.order.domain.event.OrderDeliveredEvent;
import com.ddd.order.domain.event.OrderPaidEvent;
import com.ddd.order.domain.event.OrderSubmittedEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 订单聚合根 — 订单管理的核心入口。
 *
 * DDD: Aggregate Root — 外部只能通过 Order 对象操作订单项。
 *      所有修改都必须通过 Order 的方法，确保业务规则（不变量）始终成立。
 *      聚合根方法返回领域事件，由 Application 层发布到消息队列。
 *
 * 业务规则:
 * 1. 订单必须至少包含 1 个 OrderItem
 * 2. 只有 PENDING 状态可以取消
 * 3. 只有 PENDING 状态可以标记支付
 * 4. 总金额 = 所有 OrderItem 金额之和
 */
public class Order extends AggregateRoot<OrderId> {

    private CustomerId customerId;
    private final List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private Address shippingAddress;
    private Instant createdAt;

    public Order(CustomerId customerId, Address shippingAddress) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.totalAmount = new Money(java.math.BigDecimal.ZERO, "CNY");
        this.status = OrderStatus.PENDING;
        this.shippingAddress = shippingAddress;
        this.createdAt = Instant.now();
    }

    /** 添加订单项 */
    public void addItem(ProductId productId, String productName, int quantity, Money unitPrice) {
        // 规则：只有 PENDING 状态的订单可以修改
        if (this.status != OrderStatus.PENDING) {
            throw new DomainException("ORDER_NOT_MODIFIABLE", "订单已提交，不可修改商品");
        }
        OrderItem item = new OrderItem(productId, productName, quantity, unitPrice);
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.getSubtotal());
    }

    /** 提交订单 — 触发 OrderSubmittedEvent */
    public OrderSubmittedEvent submit() {
        if (items.isEmpty()) {
            throw new DomainException("ORDER_EMPTY", "订单必须包含至少一个商品");
        }
        this.status = OrderStatus.PENDING;
        return new OrderSubmittedEvent(getId(), this.customerId, this.totalAmount, this.createdAt);
    }

    /** 标记已支付 */
    public OrderPaidEvent markPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new DomainException("INVALID_STATUS",
                    "只能支付待处理的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.PAID;
        return new OrderPaidEvent(getId());
    }

    /** 取消订单 */
    public OrderCancelledEvent cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new DomainException("INVALID_STATUS",
                    "只能取消待处理的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        return new OrderCancelledEvent(getId());
    }

    /** 标记已发货 */
    public void markShipped() {
        if (this.status != OrderStatus.PAID) {
            throw new DomainException("INVALID_STATUS",
                    "只能发货已支付的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.SHIPPED;
    }

    /** 标记已签收 */
    public OrderDeliveredEvent markDelivered() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new DomainException("INVALID_STATUS",
                    "只能签收已发货的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.DELIVERED;
        return new OrderDeliveredEvent(getId());
    }

    // Getters — 只读暴露
    public CustomerId getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Address getShippingAddress() { return shippingAddress; }
    public Instant getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 5: 实现订单领域事件**

```java
// order-service/src/main/java/com/ddd/order/domain/event/OrderSubmittedEvent.java
package com.ddd.order.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.order.domain.model.CustomerId;
import com.ddd.order.domain.model.Money;
import com.ddd.order.domain.model.OrderId;
import java.time.Instant;

public record OrderSubmittedEvent(
        OrderId orderId,
        CustomerId customerId,
        Money totalAmount,
        Instant occurredAt
) implements DomainEvent {
    // 使用隐式规范构造器，无需额外声明
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/event/OrderPaidEvent.java
package com.ddd.order.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.order.domain.model.OrderId;
import java.time.Instant;

public record OrderPaidEvent(OrderId orderId, Instant occurredAt) implements DomainEvent {
    public OrderPaidEvent(OrderId orderId) {
        this(orderId, Instant.now());
    }
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/event/OrderCancelledEvent.java
package com.ddd.order.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.order.domain.model.OrderId;
import java.time.Instant;

public record OrderCancelledEvent(OrderId orderId, Instant occurredAt) implements DomainEvent {
    public OrderCancelledEvent(OrderId orderId) {
        this(orderId, Instant.now());
    }
}
```

```java
// order-service/src/main/java/com/ddd/order/domain/event/OrderDeliveredEvent.java
package com.ddd.order.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.order.domain.model.OrderId;
import java.time.Instant;

public record OrderDeliveredEvent(OrderId orderId, Instant occurredAt) implements DomainEvent {
    public OrderDeliveredEvent(OrderId orderId) {
        this(orderId, Instant.now());
    }
}
```

- [ ] **Step 6: 实现 Repository 接口**

```java
// order-service/src/main/java/com/ddd/order/domain/repository/OrderRepository.java
package com.ddd.order.domain.repository;

import com.ddd.order.domain.model.Order;
import com.ddd.order.domain.model.OrderId;
import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口 — 定义在 Domain 层，实现在 Infrastructure 层。
 *
 * DDD: Repository 接口属于 Domain 层，因为它是聚合持久化的抽象。
 *      实现细节（JPA/MyBatis/etc）对领域层不可见。
 */
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    void save(Order order);
    List<Order> findAll();
}
```

- [ ] **Step 7: 运行业务层测试**

```bash
cd /Users/hhh/project/dddDemo
mvn test -pl order-service -q
```
Expected: All tests pass (Money + Order)

- [ ] **Step 8: 提交**

```bash
git add order-service/src/main/java/com/ddd/order/domain/
git add order-service/src/test/java/com/ddd/order/domain/
git commit -m "feat(order): 实现订单 Domain 层 — Entity, VO, Aggregate, DomainEvent, Repository"
```

---

### Task 3: Product Service — Domain 层

**Files:**
- Create: `product-service/src/main/java/com/ddd/product/domain/model/ProductId.java`
- Create: `product-service/src/main/java/com/ddd/product/domain/model/Stock.java`
- Create: `product-service/src/main/java/com/ddd/product/domain/model/ProductStatus.java`
- Create: `product-service/src/main/java/com/ddd/product/domain/model/Product.java`
- Create: `product-service/src/main/java/com/ddd/product/domain/event/StockChangedEvent.java`
- Create: `product-service/src/main/java/com/ddd/product/domain/repository/ProductRepository.java`
- Create: `product-service/src/test/java/com/ddd/product/domain/model/ProductTest.java`

- [ ] **Step 1: TDD — 写 Product 聚合测试**

```java
// product-service/src/test/java/com/ddd/product/domain/model/ProductTest.java
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
```

- [ ] **Step 2: 实现 Product 领域模型**

```java
// product-service/src/main/java/com/ddd/product/domain/model/ProductId.java
package com.ddd.product.domain.model;

import com.ddd.common.domain.ValueObject;

public record ProductId(Long value) implements ValueObject {
    public ProductId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("商品ID必须为正数");
        }
    }
}
```

```java
// product-service/src/main/java/com/ddd/product/domain/model/Stock.java
package com.ddd.product.domain.model;

import com.ddd.common.domain.ValueObject;

/**
 * 库存值对象 — 跟踪可用和预留数量。
 *
 * DDD: Value Object — 封装库存数量的业务逻辑，避免原始类型滥用。
 *      availableQuantity: 实际库存
 *      reservedQuantity: 已预留但未出库的数量
 *      实际可销售数量 = availableQuantity - reservedQuantity
 */
public record Stock(int availableQuantity, int reservedQuantity) implements ValueObject {
    public Stock {
        if (availableQuantity < 0 || reservedQuantity < 0) {
            throw new IllegalArgumentException("库存数量不能为负数");
        }
        if (reservedQuantity > availableQuantity) {
            throw new IllegalArgumentException("预留数量不能超过可用数量");
        }
    }

    public Stock reserve(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("预留数量必须为正数");
        if (reservedQuantity + quantity > availableQuantity) {
            throw new IllegalArgumentException("预留数量超过可用库存");
        }
        return new Stock(availableQuantity, reservedQuantity + quantity);
    }

    public Stock release(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("释放数量必须为正数");
        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("释放数量超过预留数量");
        }
        return new Stock(availableQuantity, reservedQuantity - quantity);
    }
}
```

```java
// product-service/src/main/java/com/ddd/product/domain/model/ProductStatus.java
package com.ddd.product.domain.model;

public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK
}
```

```java
// product-service/src/main/java/com/ddd/product/domain/model/Product.java
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
        this.stock = stock.release(quantity);
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
```

```java
// product-service/src/main/java/com/ddd/product/domain/event/StockChangedEvent.java
package com.ddd.product.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.product.domain.model.ProductId;
import com.ddd.product.domain.model.Stock;
import java.time.Instant;

public record StockChangedEvent(
        ProductId productId,
        int quantityChange,
        Stock currentStock,
        Instant occurredAt
) implements DomainEvent {
    public StockChangedEvent(ProductId productId, int quantityChange, Stock currentStock) {
        this(productId, quantityChange, currentStock, Instant.now());
    }
}
```

```java
// product-service/src/main/java/com/ddd/product/domain/repository/ProductRepository.java
package com.ddd.product.domain.repository;

import com.ddd.product.domain.model.Product;
import com.ddd.product.domain.model.ProductId;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);
    void save(Product product);
    List<Product> findAll();
}
```

- [ ] **Step 3: 运行测试**

```bash
cd /Users/hhh/project/dddDemo
mvn test -pl product-service -q
```
Expected: All tests pass

- [ ] **Step 4: 提交**

```bash
git add product-service/src/main/java/com/ddd/product/domain/
git add product-service/src/test/java/com/ddd/product/domain/
git commit -m "feat(product): 实现商品 Domain 层 — Product/Stock/ProductStatus"
```

---

### Task 4: Payment Service — Domain 层

**Files:**
- Create: `payment-service/src/main/java/com/ddd/payment/domain/model/PaymentId.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/model/PaymentStatus.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/model/PaymentMethod.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/model/TransactionId.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/model/Transaction.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/model/Payment.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/event/PaymentSucceededEvent.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/event/PaymentFailedEvent.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/event/PaymentRefundedEvent.java`
- Create: `payment-service/src/main/java/com/ddd/payment/domain/repository/PaymentRepository.java`
- Create: `payment-service/src/test/java/com/ddd/payment/domain/model/PaymentTest.java`

- [ ] **Step 1: TDD — 写 Payment 聚合测试**

```java
// payment-service/src/test/java/com/ddd/payment/domain/model/PaymentTest.java
package com.ddd.payment.domain.model;

import com.ddd.common.exception.DomainException;
import com.ddd.payment.domain.event.PaymentFailedEvent;
import com.ddd.payment.domain.event.PaymentSucceededEvent;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldInitiatePayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        assertEquals(PaymentStatus.INITIATED, payment.getStatus());
    }

    @Test
    void shouldCompletePayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        PaymentSucceededEvent event = payment.complete();
        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertNotNull(event);
    }

    @Test
    void shouldFailPayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        PaymentFailedEvent event = payment.fail("余额不足");
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("余额不足", event.reason());
    }

    @Test
    void shouldRefundOnlySucceededPayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        payment.complete();
        PaymentRefundedEvent event = payment.refund();
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }

    @Test
    void shouldNotRefundFailedPayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        payment.fail("余额不足");
        assertThrows(DomainException.class, payment::refund);
    }

    @Test
    void shouldTrackTransactions() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        payment.complete();
        payment.refund();
        assertEquals(2, payment.getTransactions().size());
    }
}
```

- [ ] **Step 2: 实现 Payment 领域模型**

```java
// payment-service/src/main/java/com/ddd/payment/domain/model/PaymentId.java
package com.ddd.payment.domain.model;

import com.ddd.common.domain.ValueObject;

public record PaymentId(Long value) implements ValueObject {
    public PaymentId {
        if (value == null || value <= 0) throw new IllegalArgumentException("支付ID必须为正数");
    }
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/model/PaymentStatus.java
package com.ddd.payment.domain.model;

public enum PaymentStatus {
    INITIATED,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    REFUNDED
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/model/PaymentMethod.java
package com.ddd.payment.domain.model;

public enum PaymentMethod {
    CREDIT_CARD,
    WECHAT,
    ALIPAY
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/model/TransactionId.java
package com.ddd.payment.domain.model;

import com.ddd.common.domain.ValueObject;

public record TransactionId(Long value) implements ValueObject {
    public TransactionId {
        if (value == null || value <= 0) throw new IllegalArgumentException("交易ID必须为正数");
    }
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/model/Transaction.java
package com.ddd.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 交易流水实体 — 记录每笔支付/退款操作。
 *
 * DDD: Entity — 有唯一标识，记录不可修改，只能追加。
 */
public class Transaction {
    private Long transactionId;
    private final String type;   // PAYMENT, REFUND
    private final BigDecimal amount;
    private final String status;
    private final Instant createdAt;

    public Transaction(String type, BigDecimal amount, String status) {
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long id) { this.transactionId = id; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/model/Payment.java
package com.ddd.payment.domain.model;

import com.ddd.common.domain.AggregateRoot;
import com.ddd.common.exception.DomainException;
import com.ddd.payment.domain.event.PaymentFailedEvent;
import com.ddd.payment.domain.event.PaymentRefundedEvent;
import com.ddd.payment.domain.event.PaymentSucceededEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 支付聚合根 — 处理支付生命周期。
 *
 * DDD: Aggregate Root — 支付操作必须通过聚合根方法。
 *
 * 业务规则:
 * 1. 一笔支付对应一个订单
 * 2. 支付成功后才能退款
 * 3. 交易流水不可修改，只能追加
 */
public class Payment extends AggregateRoot<PaymentId> {

    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private final List<Transaction> transactions;
    private Instant createdAt;

    public Payment(Long orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.INITIATED;
        this.transactions = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public PaymentSucceededEvent complete() {
        if (this.status != PaymentStatus.INITIATED && this.status != PaymentStatus.PROCESSING) {
            throw new DomainException("INVALID_STATUS",
                    "当前支付状态不可完成: " + this.status);
        }
        this.status = PaymentStatus.SUCCEEDED;
        this.transactions.add(new Transaction("PAYMENT", amount, "SUCCESS"));
        return new PaymentSucceededEvent(getId(), this.orderId);
    }

    public PaymentFailedEvent fail(String reason) {
        if (this.status == PaymentStatus.SUCCEEDED) {
            throw new DomainException("INVALID_STATUS", "已成功的支付不可标记失败");
        }
        this.status = PaymentStatus.FAILED;
        this.transactions.add(new Transaction("PAYMENT", amount, "FAILED"));
        return new PaymentFailedEvent(getId(), this.orderId, reason);
    }

    public PaymentRefundedEvent refund() {
        if (this.status != PaymentStatus.SUCCEEDED) {
            throw new DomainException("INVALID_STATUS",
                    "只能退款已成功的支付，当前状态: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.transactions.add(new Transaction("REFUND", amount, "SUCCESS"));
        return new PaymentRefundedEvent(getId(), this.orderId);
    }

    public void setMethod(PaymentMethod method) { this.method = method; }

    // Getters
    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public PaymentMethod getMethod() { return method; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public Instant getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 3: 实现支付领域事件**

```java
// payment-service/src/main/java/com/ddd/payment/domain/event/PaymentSucceededEvent.java
package com.ddd.payment.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.payment.domain.model.PaymentId;
import java.time.Instant;

public record PaymentSucceededEvent(PaymentId paymentId, Long orderId, Instant occurredAt) implements DomainEvent {
    public PaymentSucceededEvent(PaymentId paymentId, Long orderId) {
        this(paymentId, orderId, Instant.now());
    }
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/event/PaymentFailedEvent.java
package com.ddd.payment.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.payment.domain.model.PaymentId;
import java.time.Instant;

public record PaymentFailedEvent(PaymentId paymentId, Long orderId, String reason, Instant occurredAt) implements DomainEvent {
    public PaymentFailedEvent(PaymentId paymentId, Long orderId, String reason) {
        this(paymentId, orderId, reason, Instant.now());
    }
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/event/PaymentRefundedEvent.java
package com.ddd.payment.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.payment.domain.model.PaymentId;
import java.time.Instant;

public record PaymentRefundedEvent(PaymentId paymentId, Long orderId, Instant occurredAt) implements DomainEvent {
    public PaymentRefundedEvent(PaymentId paymentId, Long orderId) {
        this(paymentId, orderId, Instant.now());
    }
}
```

```java
// payment-service/src/main/java/com/ddd/payment/domain/repository/PaymentRepository.java
package com.ddd.payment.domain.repository;

import com.ddd.payment.domain.model.Payment;
import com.ddd.payment.domain.model.PaymentId;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(PaymentId id);
    void save(Payment payment);
    Optional<Payment> findByOrderId(Long orderId);
}
```

- [ ] **Step 4: 运行测试**

```bash
cd /Users/hhh/project/dddDemo
mvn test -pl payment-service -q
```
Expected: All tests pass

- [ ] **Step 5: 提交**

```bash
git add payment-service/src/main/java/com/ddd/payment/domain/
git add payment-service/src/test/java/com/ddd/payment/domain/
git commit -m "feat(payment): 实现支付 Domain 层 — Payment/Transaction/PaymentStatus"
```

---

### Task 5: 各服务 Infrastructure + REST + Application 层

**Files:**
- Create: `order-service/src/main/java/com/ddd/order/OrderApplication.java`
- Create: `order-service/src/main/java/com/ddd/order/application/dto/CreateOrderRequest.java`
- Create: `order-service/src/main/java/com/ddd/order/application/dto/OrderResponse.java`
- Create: `order-service/src/main/java/com/ddd/order/application/assembler/OrderAssembler.java`
- Create: `order-service/src/main/java/com/ddd/order/application/service/OrderApplicationService.java`
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/persistence/jpa/OrderJpaEntity.java`
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/persistence/jpa/OrderItemJpaEntity.java`
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/persistence/jpa/OrderJpaRepository.java`
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/persistence/OrderRepositoryImpl.java`
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/rest/OrderController.java`
- Create: `order-service/src/main/resources/application.yml`
- product-service 和 payment-service 的同构文件

- [ ] **Step 1: 创建 Order Service 启动类**

```java
// order-service/src/main/java/com/ddd/order/OrderApplication.java
package com.ddd.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

- [ ] **Step 2: 创建 Application DTO**

```java
// order-service/src/main/java/com/ddd/order/application/dto/CreateOrderRequest.java
package com.ddd.order.application.dto;

import com.ddd.order.domain.model.Money;
import java.util.List;

/** 创建订单请求 DTO — Application 层入参 */
public record CreateOrderRequest(
        Long customerId,
        String street,
        String city,
        String zipCode,
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(Long productId, String productName, int quantity, Money unitPrice) {}
}
```

```java
// order-service/src/main/java/com/ddd/order/application/dto/OrderResponse.java
package com.ddd.order.application.dto;

import com.ddd.order.domain.model.Address;
import com.ddd.order.domain.model.Money;
import com.ddd.order.domain.model.OrderId;
import com.ddd.order.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;

/** 订单响应 DTO — Application 层出参 */
public record OrderResponse(
        OrderId orderId,
        Long customerId,
        Money totalAmount,
        OrderStatus status,
        Address shippingAddress,
        List<OrderItemResponse> items,
        Instant createdAt
) {
    public record OrderItemResponse(
            Long productId, String productName, int quantity, Money unitPrice, Money subtotal
    ) {}
}
```

- [ ] **Step 3: 创建 Assembler**

```java
// order-service/src/main/java/com/ddd/order/application/assembler/OrderAssembler.java
package com.ddd.order.application.assembler;

import com.ddd.order.application.dto.CreateOrderRequest;
import com.ddd.order.application.dto.OrderResponse;
import com.ddd.order.domain.model.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单 Assembler — 在 Application 层做 DTO 与 Domain Model 之间的双向转换。
 *
 * DDD: Anti-Corruption Layer (防腐层) 的一部分。
 *      确保外部模型（DTO）不会污染领域模型，反之亦然。
 */
public class OrderAssembler {

    public static Order toDomain(CreateOrderRequest request) {
        Order order = new Order(
                new CustomerId(request.customerId()),
                new Address(request.street(), request.city(), request.zipCode())
        );
        for (var item : request.items()) {
            order.addItem(
                    new ProductId(item.productId()),
                    item.productName(),
                    item.quantity(),
                    item.unitPrice()
            );
        }
        return order;
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getProductId().value(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCustomerId().value(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                items,
                order.getCreatedAt()
        );
    }
}
```

- [ ] **Step 4: 创建 Application Service**

```java
// order-service/src/main/java/com/ddd/order/application/service/OrderApplicationService.java
package com.ddd.order.application.service;

import com.ddd.order.application.assembler.OrderAssembler;
import com.ddd.order.application.dto.CreateOrderRequest;
import com.ddd.order.application.dto.OrderResponse;
import com.ddd.order.domain.model.Order;
import com.ddd.order.domain.model.OrderId;
import com.ddd.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单应用服务 — 流程编排，不包含业务逻辑。
 *
 * DDD: Application Service 是 Domain 层的直接客户。
 *      职责: 事务管理、调用 Domain 方法、发布事件、DTO 转换。
 *      注意: 不包含 if/else 业务判断，所有业务规则在 Domain 层。
 */
@Service
@Transactional
public class OrderApplicationService {

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = OrderAssembler.toDomain(request);
        order.submit();
        orderRepository.save(order);
        return OrderAssembler.toResponse(order);
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(new OrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
        return OrderAssembler.toResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderAssembler::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(new OrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
        order.cancel();
        orderRepository.save(order);
        return OrderAssembler.toResponse(order);
    }

    @Transactional
    public OrderResponse payOrder(Long orderId) {
        Order order = orderRepository.findById(new OrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
        order.markPaid();
        orderRepository.save(order);
        return OrderAssembler.toResponse(order);
    }
}
```

- [ ] **Step 5: 创建 JPA 实体与 Repository 实现**

```java
// order-service/src/main/java/com/ddd/order/infrastructure/persistence/jpa/OrderJpaEntity.java
package com.ddd.order.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status;

    private String shippingAddress;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItemJpaEntity> getItems() { return items; }
    public void setItems(List<OrderItemJpaEntity> items) { this.items = items; }
}
```

```java
// order-service/src/main/java/com/ddd/order/infrastructure/persistence/jpa/OrderItemJpaEntity.java
package com.ddd.order.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
```

```java
// order-service/src/main/java/com/ddd/order/infrastructure/persistence/jpa/OrderJpaRepository.java
package com.ddd.order.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA 接口 — Infrastructure 层的技术实现。
 * 这个接口对 Domain 层完全不可见。
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
}
```

```java
// order-service/src/main/java/com/ddd/order/infrastructure/persistence/OrderRepositoryImpl.java
package com.ddd.order.infrastructure.persistence;

import com.ddd.order.domain.model.*;
import com.ddd.order.domain.repository.OrderRepository;
import com.ddd.order.infrastructure.persistence.jpa.OrderItemJpaEntity;
import com.ddd.order.infrastructure.persistence.jpa.OrderJpaEntity;
import com.ddd.order.infrastructure.persistence.jpa.OrderJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单仓储实现 — 将 Domain 对象转换为 JPA 实体。
 *
 * DDD: Repository 接口定义在 Domain 层，实现在 Infrastructure 层。
 *      这种分离确保 Domain 层不依赖任何持久化技术。
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = toJpa(order);
        if (order.getId() != null) {
            entity.setId(order.getId().value());
        }
        OrderJpaEntity saved = jpaRepository.save(entity);
        order.setId(new OrderId(saved.getId()));
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Order toDomain(OrderJpaEntity entity) {
        Order order = new Order(
                new CustomerId(entity.getCustomerId()),
                new Address(entity.getShippingAddress(), "", "")
        );
        order.setId(new OrderId(entity.getId()));
        // 回填 Order 内部状态（包级私有方法，Infrastructure 可访问）
        OrderStatus status = OrderStatus.valueOf(entity.getStatus());
        Money totalAmount = new Money(entity.getTotalAmount(), entity.getCurrency());
        // 通过反射设置私有字段 — 因为 Order 没有公开的 setStatus/setTotalAmount
        // 注意: 实际生产可用 Builder 模式或构造器重载，此处为保持 Domain 纯净使用反射
        try {
            var statusField = Order.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, status);

            var totalField = Order.class.getDeclaredField("totalAmount");
            totalField.setAccessible(true);
            totalField.set(order, totalAmount);
        } catch (Exception e) {
            throw new RuntimeException("Order 领域对象重建失败", e);
        }
        return order;
    }

    private OrderJpaEntity toJpa(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        if (order.getId() != null) {
            entity.setId(order.getId().value());
        }
        entity.setCustomerId(order.getCustomerId().value());
        entity.setTotalAmount(order.getTotalAmount().amount());
        entity.setCurrency(order.getTotalAmount().currency());
        entity.setStatus(order.getStatus().name());
        entity.setShippingAddress(order.getShippingAddress().street());
        entity.setCreatedAt(order.getCreatedAt());

        List<OrderItemJpaEntity> items = order.getItems().stream().map(item -> {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity();
            itemEntity.setProductId(item.getProductId().value());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice().amount());
            itemEntity.setSubtotal(item.getSubtotal().amount());
            return itemEntity;
        }).collect(Collectors.toList());
        entity.setItems(items);
        return entity;
    }
}
```

- [ ] **Step 6: 创建 REST Controller**

```java
// order-service/src/main/java/com/ddd/order/infrastructure/rest/OrderController.java
package com.ddd.order.infrastructure.rest;

import com.ddd.order.application.dto.CreateOrderRequest;
import com.ddd.order.application.dto.OrderResponse;
import com.ddd.order.application.service.OrderApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        return orderApplicationService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderApplicationService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderApplicationService.getAllOrders();
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderApplicationService.cancelOrder(id);
    }

    @PostMapping("/{id}/pay")
    public OrderResponse payOrder(@PathVariable Long id) {
        return orderApplicationService.payOrder(id);
    }
}
```

- [ ] **Step 7: 创建配置文件**

```yaml
# order-service/src/main/resources/application.yml
server:
  port: 8081

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:mysql://localhost:3306/ddd_order?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

- [ ] **Step 8: 创建全局异常处理器（所有服务共用）**

为每个服务创建 `GlobalExceptionHandler`（放在各自 infrastructure/rest 包下），统一错误响应格式：

```java
// order-service/src/main/java/com/ddd/order/infrastructure/rest/GlobalExceptionHandler.java
package com.ddd.order.infrastructure.rest;

import com.ddd.common.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(DomainException ex) {
        log.warn("业务规则违反: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "code", ex.getCode(),
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "code", "BAD_REQUEST",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
        log.error("未预期异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "code", "INTERNAL_ERROR",
                "message", "系统内部错误",
                "timestamp", Instant.now().toString()
        ));
    }
}
```

Product 和 Payment 服务使用同构的 GlobalExceptionHandler。

- [ ] **Step 9: 为 Product 和 Payment 服务创建同构的 Application + Infrastructure 层**

product-service 的类似结构（文件路径同构替换 product 即可）：
- `ProductApplication.java` — 启动类，端口 8082
- `ProductApplicationService.java` — 查询商品、列表
- `ProductController.java` — GET `/api/products/{id}`, GET `/api/products`
- `application.yml` — 数据源指向 `ddd_product`

payment-service 的类似结构：
- `PaymentApplication.java` — 启动类，端口 8083
- `PaymentApplicationService.java` — 支付发起、查询、退款
- `PaymentController.java` — POST `/api/payments`, GET `/api/payments/{id}`, POST `/api/payments/{id}/refund`
- `application.yml` — 数据源指向 `ddd_payment`

- [ ] **Step 9: 编译验证**

```bash
cd /Users/hhh/project/dddDemo
mvn clean compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 10: 提交**

```bash
git add order-service/src/main/java/com/ddd/order/application/
git add order-service/src/main/java/com/ddd/order/infrastructure/
git add order-service/src/main/resources/
git add product-service/src/main/java/com/ddd/product/application/
git add product-service/src/main/java/com/ddd/product/infrastructure/
git add product-service/src/main/resources/
git add payment-service/src/main/java/com/ddd/payment/application/
git add payment-service/src/main/java/com/ddd/payment/infrastructure/
git add payment-service/src/main/resources/
git commit -m "feat: 添加各服务的 Application、Infrastructure 和 REST API 层"
```

---

### Task 6: Discovery 与 Gateway 服务

**Files:**
- Create: `discovery-service/src/main/java/com/ddd/discovery/DiscoveryApplication.java`
- Create: `discovery-service/src/main/resources/application.yml`
- Create: `gateway-service/src/main/java/com/ddd/gateway/GatewayApplication.java`
- Create: `gateway-service/src/main/resources/application.yml`

- [ ] **Step 1: 创建 Eureka Server**

```java
// discovery-service/src/main/java/com/ddd/discovery/DiscoveryApplication.java
package com.ddd.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryApplication.class, args);
    }
}
```

```yaml
# discovery-service/src/main/resources/application.yml
server:
  port: 8761

spring:
  application:
    name: discovery-service

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

- [ ] **Step 2: 创建 Gateway**

```java
// gateway-service/src/main/java/com/ddd/gateway/GatewayApplication.java
package com.ddd.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

```yaml
# gateway-service/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

- [ ] **Step 3: 编译验证**

```bash
cd /Users/hhh/project/dddDemo
mvn clean compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add discovery-service/ gateway-service/
git commit -m "feat: 添加 Eureka 注册中心和 API 网关"
```

---

### Task 7: RabbitMQ 领域事件集成

**Files:**
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/messaging/RabbitMQConfig.java`
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/messaging/OrderEventPublisher.java`
- Create: `order-service/src/main/java/com/ddd/order/infrastructure/messaging/PaymentEventConsumer.java`
- Create: `product-service/src/main/java/com/ddd/product/infrastructure/messaging/RabbitMQConfig.java`
- Create: `product-service/src/main/java/com/ddd/product/infrastructure/messaging/OrderEventConsumer.java`
- Create: `payment-service/src/main/java/com/ddd/payment/infrastructure/messaging/RabbitMQConfig.java`
- Create: `payment-service/src/main/java/com/ddd/payment/infrastructure/messaging/OrderEventConsumer.java`

- [ ] **Step 1: 在 common 模块中创建跨服务共享事件消息**

```java
// common/src/main/java/com/ddd/common/event/OrderSubmittedMessage.java
package com.ddd.common.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 跨服务共享的订单提交消息 — 通过 RabbitMQ 序列化传输。
 * 注意：这不是领域事件！领域事件定义在每个服务的 Domain 层。
 * 这是 Application 层的事件消息 DTO，用于跨限界上下文通讯。
 */
public record OrderSubmittedMessage(
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        String currency
) {
    public Instant occurredAt() { return Instant.now(); }
}
```

```java
// common/src/main/java/com/ddd/common/event/PaymentSucceededMessage.java
package com.ddd.common.event;

public record PaymentSucceededMessage(Long paymentId, Long orderId) {}
```

```java
// common/src/main/java/com/ddd/common/event/PaymentFailedMessage.java
package com.ddd.common.event;

public record PaymentFailedMessage(Long paymentId, Long orderId, String reason) {}
```

```java
// common/src/main/java/com/ddd/common/event/OrderCancelledMessage.java
package com.ddd.common.event;

public record OrderCancelledMessage(Long orderId) {}
```

- [ ] **Step 2: 定义 RabbitMQ 事件常量与公共配置**

RabbitMQ 使用统一的事件路由结构：
```
exchange: ddd.order.events
routing keys: order.submitted, order.cancelled, payment.succeeded, payment.failed
```

- [ ] **Step 3: Order Service — RabbitMQ 配置**

```java
// order-service/src/main/java/com/ddd/order/infrastructure/messaging/RabbitMQConfig.java
package com.ddd.order.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "ddd.order.events";
    public static final String ORDER_SUBMITTED_QUEUE = "order.submitted.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";
    public static final String ORDER_PAID_EVENTS_QUEUE = "order.paid.events.queue";

    public static final String PAYMENT_SUCCEEDED_QUEUE = "payment.succeeded.order.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.order.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // Order 发布的事件队列
    @Bean
    public Queue orderSubmittedQueue() { return new Queue(ORDER_SUBMITTED_QUEUE); }
    @Bean
    public Queue orderCancelledQueue() { return new Queue(ORDER_CANCELLED_QUEUE); }
    @Bean
    public Queue orderPaidEventsQueue() { return new Queue(ORDER_PAID_EVENTS_QUEUE); }

    // Order 消费的事件队列
    @Bean
    public Queue paymentSucceededQueue() { return new Queue(PAYMENT_SUCCEEDED_QUEUE); }
    @Bean
    public Queue paymentFailedQueue() { return new Queue(PAYMENT_FAILED_QUEUE); }

    @Bean
    public Binding orderSubmittedBinding() {
        return BindingBuilder.bind(orderSubmittedQueue()).to(orderExchange()).with("order.submitted");
    }
    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue()).to(orderExchange()).with("order.cancelled");
    }
    @Bean
    public Binding orderPaidEventsBinding() {
        return BindingBuilder.bind(orderPaidEventsQueue()).to(orderExchange()).with("order.paid");
    }
    @Bean
    public Binding paymentSucceededBinding() {
        return BindingBuilder.bind(paymentSucceededQueue()).to(orderExchange()).with("payment.succeeded");
    }
    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue()).to(orderExchange()).with("payment.failed");
    }
}
```

- [ ] **Step 4: Order Service — 事件发布（使用共享消息 DTO）**

```java
// order-service/src/main/java/com/ddd/order/infrastructure/messaging/OrderEventPublisher.java
package com.ddd.order.infrastructure.messaging;

import com.ddd.common.event.OrderCancelledMessage;
import com.ddd.common.event.OrderSubmittedMessage;
import com.ddd.order.domain.event.OrderCancelledEvent;
import com.ddd.order.domain.event.OrderPaidEvent;
import com.ddd.order.domain.event.OrderSubmittedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单事件发布器 — 将 Domain Event 转换为跨服务共享 Message DTO 后发送。
 *
 * DDD: Application 层负责将 Domain Event 转换为跨上下文的消息。
 *      避免领域事件直接暴露给外部限界上下文。
 */
@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderSubmittedEvent event) {
        OrderSubmittedMessage msg = new OrderSubmittedMessage(
                event.orderId().value(),
                event.customerId().value(),
                event.totalAmount().amount(),
                event.totalAmount().currency()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, "order.submitted", msg);
    }

    public void publish(OrderCancelledEvent event) {
        OrderCancelledMessage msg = new OrderCancelledMessage(event.orderId().value());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, "order.cancelled", msg);
    }

    public void publish(OrderPaidEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, "order.paid",
                event.orderId().value());
    }
}
```

- [ ] **Step 5: Order Service — 消费 Payment 事件**

```java
// order-service/src/main/java/com/ddd/order/infrastructure/messaging/PaymentEventConsumer.java
package com.ddd.order.infrastructure.messaging;

import com.ddd.common.event.PaymentFailedMessage;
import com.ddd.order.application.service.OrderApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final OrderApplicationService orderApplicationService;

    public PaymentEventConsumer(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCEEDED_QUEUE)
    public void handlePaymentSucceeded(Long orderId) {
        log.info("支付成功事件: orderId={}", orderId);
        orderApplicationService.payOrder(orderId);
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedMessage event) {
        log.warn("支付失败事件: orderId={}, reason={}", event.orderId(), event.reason());
    }
}
```

- [ ] **Step 5: Product Service — 配置与消费 Order 事件**

```java
// product-service/src/main/java/com/ddd/product/infrastructure/messaging/RabbitMQConfig.java
package com.ddd.product.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "ddd.order.events";
    public static final String ORDER_SUBMITTED_QUEUE = "product.order.submitted.queue";
    public static final String ORDER_CANCELLED_QUEUE = "product.order.cancelled.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderSubmittedQueue() { return new Queue(ORDER_SUBMITTED_QUEUE); }
    @Bean
    public Queue orderCancelledQueue() { return new Queue(ORDER_CANCELLED_QUEUE); }

    @Bean
    public Binding orderSubmittedBinding() {
        return BindingBuilder.bind(orderSubmittedQueue()).to(orderExchange()).with("order.submitted");
    }
    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue()).to(orderExchange()).with("order.cancelled");
    }
}
```

```java
// product-service/src/main/java/com/ddd/product/infrastructure/messaging/OrderEventConsumer.java
package com.ddd.product.infrastructure.messaging;

import com.ddd.common.event.OrderCancelledMessage;
import com.ddd.common.event.OrderSubmittedMessage;
import com.ddd.product.application.service.ProductApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final ProductApplicationService productApplicationService;

    public OrderEventConsumer(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_SUBMITTED_QUEUE)
    public void handleOrderSubmitted(OrderSubmittedMessage event) {
        log.info("订单提交事件: orderId={}", event.orderId());
        // 预留库存逻辑 — 从事件中解析商品ID和数量
        // 简化实现: 调用 ProductApplicationService.reserveStock()
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledMessage event) {
        log.info("订单取消事件: orderId={}", event.orderId());
        // 释放库存逻辑
    }
}
```

- [ ] **Step 6: Payment Service — 配置与消费 Order 事件**

与 Product Service 类似的配置，监听 `order.submitted` 事件后发起支付。

```java
// payment-service/src/main/java/com/ddd/payment/infrastructure/messaging/RabbitMQConfig.java
package com.ddd.payment.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "ddd.order.events";
    public static final String PAYMENT_EXCHANGE = "ddd.payment.events";

    public static final String ORDER_SUBMITTED_QUEUE = "payment.order.submitted.queue";
    public static final String PAYMENT_SUCCEEDED_QUEUE = "payment.succeeded.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";

    @Bean
    public TopicExchange orderExchange() { return new TopicExchange(ORDER_EXCHANGE); }
    @Bean
    public TopicExchange paymentExchange() { return new TopicExchange(PAYMENT_EXCHANGE); }

    @Bean
    public Queue orderSubmittedQueue() { return new Queue(ORDER_SUBMITTED_QUEUE); }
    @Bean
    public Queue paymentSucceededQueue() { return new Queue(PAYMENT_SUCCEEDED_QUEUE); }
    @Bean
    public Queue paymentFailedQueue() { return new Queue(PAYMENT_FAILED_QUEUE); }

    @Bean
    public Binding orderSubmittedBinding() {
        return BindingBuilder.bind(orderSubmittedQueue()).to(orderExchange()).with("order.submitted");
    }
    @Bean
    public Binding paymentSucceededBinding() {
        return BindingBuilder.bind(paymentSucceededQueue()).to(paymentExchange()).with("payment.succeeded");
    }
    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue()).to(paymentExchange()).with("payment.failed");
    }
}
```

- [ ] **Step 7: 编译验证**

```bash
cd /Users/hhh/project/dddDemo
mvn clean compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 8: 提交**

```bash
git add common/src/main/java/com/ddd/common/event/
git add order-service/src/main/java/com/ddd/order/infrastructure/messaging/
git add product-service/src/main/java/com/ddd/product/infrastructure/messaging/
git add payment-service/src/main/java/com/ddd/payment/infrastructure/messaging/
git commit -m "feat: 集成 RabbitMQ 领域事件 — 事件发布与消费"
```

---

### Task 8: Docker Compose 与端到端流程验证

**Files:**
- Create: `docker-compose.yml`
- Create: `init.sql`

- [ ] **Step 1: 创建 Docker Compose**

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: ddd-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_ROOT_HOST: '%'
    ports:
      - "3306:3306"
    command: --default-authentication-plugin=mysql_native_password
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: ddd-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    healthcheck:
      test: ["CMD", "rabbitmqctl", "status"]
      interval: 10s
      timeout: 5s
      retries: 5

  discovery-service:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        SERVICE: discovery-service
    container_name: ddd-discovery
    ports:
      - "8761:8761"

  order-service:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        SERVICE: order-service
    container_name: ddd-order
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ddd_order?createDatabaseIfNotExist=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_RABBITMQ_HOST: rabbitmq
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://discovery-service:8761/eureka/
    depends_on:
      mysql: { condition: service_healthy }
      rabbitmq: { condition: service_healthy }
      discovery-service: { condition: service_started }

  product-service:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        SERVICE: product-service
    container_name: ddd-product
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ddd_product?createDatabaseIfNotExist=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_RABBITMQ_HOST: rabbitmq
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://discovery-service:8761/eureka/
    depends_on:
      mysql: { condition: service_healthy }
      rabbitmq: { condition: service_healthy }
      discovery-service: { condition: service_started }

  payment-service:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        SERVICE: payment-service
    container_name: ddd-payment
    ports:
      - "8083:8083"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ddd_payment?createDatabaseIfNotExist=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_RABBITMQ_HOST: rabbitmq
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://discovery-service:8761/eureka/
    depends_on:
      mysql: { condition: service_healthy }
      rabbitmq: { condition: service_healthy }
      discovery-service: { condition: service_started }

  gateway-service:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        SERVICE: gateway-service
    container_name: ddd-gateway
    ports:
      - "8080:8080"
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://discovery-service:8761/eureka/
    depends_on:
      - discovery-service

volumes:
  mysql-data:
```

- [ ] **Step 2: 创建 Dockerfile**

```dockerfile
# Dockerfile (项目根目录)
FROM eclipse-temurin:17-jdk-alpine
ARG SERVICE
COPY ${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

- [ ] **Step 3: 验证完整编译**

```bash
cd /Users/hhh/project/dddDemo
mvn clean package -DskipTests -q
```
Expected: BUILD SUCCESS（各服务生成 target/*.jar）

- [ ] **Step 4: 提交**

```bash
git add docker-compose.yml Dockerfile
git commit -m "feat: 添加 Docker Compose 编排，支持全服务容器化启动"
```

---

### Task 9: README 与 DDD 教学文档

**Files:**
- Create: `README.md`（中英双语 DDD 学习指南）

- [ ] **Step 1: 创建 README**

```markdown
# DDD Demo — 订单管理系统 (Order Management System)

一个基于 **Domain-Driven Design (DDD)** 构建的订单管理系统 Demo，旨在通过可运行项目展示 DDD 的 **战略设计** 与 **战术设计** 核心概念。

## 项目架构

```
ddd-demo/
├── common/              # 共享领域抽象（纯 Java，无框架依赖）
├── order-service/       # 订单上下文 - 端口 8081
├── product-service/     # 商品上下文 - 端口 8082
├── payment-service/     # 支付上下文 - 端口 8083
├── discovery-service/   # Eureka 注册中心 - 端口 8761
├── gateway-service/     # API 网关 - 端口 8080
└── docker-compose.yml   # 基础设施编排
```

## DDD 核心概念讲解

### 1. 战略设计 (Strategic Design)

| 概念 | 说明 | 本项目体现 |
|------|------|-----------|
| **限界上下文** | 每个上下文有独立的模型和语言 | `order-service`, `product-service`, `payment-service` 各为独立上下文 |
| **统一语言** | 团队与代码使用相同的业务术语 | 类名如 `Order`, `Product`, `Payment` 与业务术语完全一致 |
| **上下文映射** | 定义上下文之间的关系 | Order ↔ Product ↔ Payment 通过领域事件协作 |

### 2. 战术设计 (Tactical Design)

| 概念 | 说明 | 代码位置 |
|------|------|---------|
| **Entity** | 有唯一标识，可变 | `Order`, `OrderItem`, `Product`, `Payment`, `Transaction` |
| **Value Object** | 无标识，不可变，通过值比较 | `Money`, `Address`, `OrderId`, `Stock`, `OrderStatus` |
| **Aggregate Root** | 聚合入口，保证不变量 | `Order`, `Product`, `Payment` |
| **Domain Event** | 过去发生的业务事实 | `OrderSubmittedEvent`, `PaymentSucceededEvent` |
| **Repository** | 聚合的持久化抽象 | `OrderRepository`(接口) → `OrderRepositoryImpl`(JPA实现) |
| **Domain Service** | 跨多个实体的业务逻辑 | `OrderDomainService` |
| **Application Service** | 流程编排，无业务逻辑 | `OrderApplicationService` |

### 3. 六边形架构 (Hexagonal Architecture)

```
Interface/API → Application → Domain ← Infrastructure
                                      (实现 Domain 接口)
```

**核心原则:** Domain 层零外部依赖，所有技术细节（JPA、MQ、REST）都在 Infrastructure 层。

## 快速启动

### 前置条件

- Docker & Docker Compose
- Java 17+ (本地编译用)
- Maven 3.8+ (本地编译用)

### 启动步骤

```bash
# 1. 编译项目
mvn clean package -DskipTests

# 2. 启动所有服务
docker-compose up -d

# 3. 访问服务
# API 网关: http://localhost:8080
# Eureka:   http://localhost:8761
# RabbitMQ: http://localhost:15672 (guest/guest)
```

### API 测试

```bash
# 创建订单
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"items":[{"productId":1,"productName":"商品A","quantity":2,"unitPrice":{"amount":100.00,"currency":"CNY"}}]}'

# 查询订单
curl http://localhost:8080/api/orders/1

# 取消订单
curl -X POST http://localhost:8080/api/orders/1/cancel
```

## 学习路径建议

1. **先读** `docs/superpowers/specs/2026-05-17-ddd-order-demo-design.md` — 理解整体设计
2. **按顺序阅读 Domain 层** — Order → Product → Payment
3. **理解六边形架构** — 观察 Domain 如何零依赖外部框架
4. **追踪事件流** — 一个订单从创建到完成的完整异步流程
5. **运行并测试** — 通过 API 验证业务规则
```

- [ ] **Step 2: 最终提交**

```bash
git add README.md
git commit -m "docs: 添加 DDD 学习文档与项目 README"
```

---

### Task 10: 最终验证

- [ ] **Step 1: 运行所有测试**

```bash
cd /Users/hhh/project/dddDemo
mvn clean test -q
```
Expected: BUILD SUCCESS — 所有 Domain 层测试通过

- [ ] **Step 2: 完整编译**

```bash
mvn clean package -DskipTests -q
```
Expected: BUILD SUCCESS — 各服务生成可执行 JAR

- [ ] **Step 3: 验证项目文件完整性**

确保以下关键文件全部存在：

```bash
ls -d */
# common/ order-service/ product-service/ payment-service/ discovery-service/ gateway-service/

ls docker-compose.yml Dockerfile README.md pom.xml .gitignore
# All should exist
```
