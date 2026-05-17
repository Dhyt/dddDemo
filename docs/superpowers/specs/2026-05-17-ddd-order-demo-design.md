# DDD 订单管理系统 Demo 设计文档

## 概述

基于 Domain-Driven Design（领域驱动设计）构建的订单管理系统 Demo，旨在通过完整的可运行项目展示 DDD 的战略设计与战术设计核心概念。项目使用 Java + Spring Boot 技术栈，采用微服务 + MySQL + RabbitMQ 架构。

## 核心目标

1. 展示 DDD 完整实践路径：战略设计 → 战术设计 → 代码落地
2. 体现 Ubiquitous Language（统一语言）在代码中的映射
3. 演示 Bounded Context（限界上下文）之间的协作
4. 覆盖核心 DDD 模式：Aggregate、Value Object、Domain Event、Repository、Domain Service
5. 提供详细的流程说明和概念解释，作为学习 DDD 的参考教材

---

## 一、战略设计 (Strategic Design)

### 1.1 限界上下文 (Bounded Context)

系统拆分为 3 个限界上下文，每个独立为微服务：

| 限界上下文 | 微服务 | 核心职责 | 数据库 Schema |
|-----------|--------|---------|-------------|
| Order Context | order-service | 订单生命周期管理 | `ddd_order` |
| Product Context | product-service | 商品与库存管理 | `ddd_product` |
| Payment Context | payment-service | 支付处理与交易记录 | `ddd_payment` |

**限界上下文划分原则：**
- 每个上下文有独立的统一语言（术语表）
- 每个上下文有独立的业务规则和不变量
- 上下文之间通过领域事件异步通讯，降低耦合
- 每个上下文拥有自己的数据存储，避免共享数据库

### 1.2 统一语言 (Ubiquitous Language)

详见设计讨论中记录的术语表。关键原则：
- 术语必须在代码中保持一致（类名、方法名、变量名）
- 术语定义在每个上下文内部生效
- 跨上下文引用时使用 Anticorruption Layer（防腐层）进行翻译

### 1.3 上下文映射 (Context Map)

```
[Order Context] ──(事件)──▶ [Product Context]
       │                          ▲
       │                          │
       ▼                          │
[Payment Context] ──(事件)────────┘
```

关系类型：Partner/Cooperative（通过领域事件协作，各方独立演化事件模型）

---

## 二、战术设计 (Tactical Design)

### 2.1 聚合 (Aggregate) 设计

#### Order 聚合（order-service）

```
Order (Aggregate Root)
├── OrderId (Value Object)          # 订单ID
├── CustomerId (Value Object)       # 客户ID
├── Money totalAmount (Value Object)# 总金额
├── OrderStatus status (Value Object)# 订单状态
├── Address shippingAddress (VO)    # 收货地址
├── List<OrderItem> items           # 订单项（实体）
└── Instant createdAt               # 创建时间

OrderItem (Entity)
├── OrderItemId                     # 行ID
├── ProductId (VO)                  # 商品ID
├── productName                     # 商品快照名称
├── int quantity                    # 数量
├── Money unitPrice                 # 单价
└── Money subtotal                  # 小计
```

**聚合业务规则 (Invariants)：**
1. 订单必须包含至少 1 个 OrderItem
2. 只有 PENDING 状态的订单可以取消
3. 只有 PENDING 状态的订单可以标记为已支付
4. 总金额始终等于所有 OrderItem 金额之和
5. OrderItem 不可被外部直接修改，必须通过 Order 的方法操作

#### Product 聚合（product-service）

```
Product (Aggregate Root)
├── ProductId (Value Object)
├── name, description
├── Money price
├── Stock stock (Value Object)
│   ├── int availableQuantity
│   └── int reservedQuantity
├── Category category
└── ProductStatus status
```

**业务规则：**
1. 预留库存不能超过可用库存（`availableQuantity >= reservedQuantity`）
2. 商品下架（INACTIVE）后不可被新订单引用
3. 库存操作通过 `reserveStock()` / `releaseStock()` 方法，不允许直接修改

#### Payment 聚合（payment-service）

```
Payment (Aggregate Root)
├── PaymentId (Value Object)
├── OrderId (Value Object)
├── Money amount
├── PaymentStatus status
├── PaymentMethod method
└── List<Transaction> transactions
```

**业务规则：**
1. 一笔支付对应一个订单
2. 支付成功后才能退款
3. 交易流水不可修改，只能追加

### 2.2 值对象 (Value Object) 设计

值对象的特征：不可变性、无身份标识、通过属性值比较相等性。

```java
// Money 值对象示例
public class Money {
    private final BigDecimal amount;
    private final String currency; // "CNY", "USD"

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("不同币种不可相加");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    // equals() 和 hashCode() 基于 amount + currency
}
```

### 2.3 领域事件 (Domain Event)

每个领域事件代表过去发生的业务事实，命名使用过去时态：

| 事件 | 发布者 | 消费者 | 触发时机 |
|------|-------|--------|---------|
| OrderSubmittedEvent | Order Service | Product, Payment | 订单提交成功 |
| OrderPaidEvent | Order Service | Payment | 订单标记已支付 |
| OrderCancelledEvent | Order Service | Product | 订单取消成功 |
| OrderDeliveredEvent | Order Service | - | 订单签收 |
| PaymentSucceededEvent | Payment Service | Order | 支付成功 |
| PaymentFailedEvent | Payment Service | Order | 支付失败 |
| PaymentRefundedEvent | Payment Service | - | 退款成功 |

---

## 三、六边形架构 (Hexagonal Architecture)

每个微服务严格遵循六边形架构（Ports & Adapters），分为四层：

### 层依赖规则

```
Interface/API 层  ──▶  Application 层  ──▶  Domain 层
       │                                         │
       └────── Infrastructure 层 ◀────────────────┘
                    (实现 Domain 接口)
```

**关键约束：**
- Domain 层零外部依赖（不依赖 Spring、JPA 等框架注解）
- Infrastructure 层依赖 Domain 层，实现 Repository 接口
- Application 层编排流程，不包含业务逻辑
- Interface 层处理 HTTP/消息入站

### 各层职责

| 层次 | 职责 | 包含 |
|------|------|------|
| Domain | 纯业务逻辑，业务规则 | Entity, VO, Aggregate, DomainEvent, DomainService, Repository接口 |
| Application | 流程编排，事务管理，事件发布 | ApplicationService, DTO, Assembler, EventConsumer |
| Infrastructure | 技术实现 | JPA Repository实现, Messaging实现, REST Controller |
| Interface | 外部通讯适配 | Controller, Listener |

---

## 四、技术架构

### 4.1 组件图

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Eureka      │     │  Gateway     │     │  RabbitMQ    │
│  Server      │     │  Service     │     │  (Events)    │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                     │
       └────────────────────┼─────────────────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
       ┌──────▼────┐ ┌─────▼──────┐ ┌────▼──────┐
       │  Order    │ │  Product   │ │  Payment  │
       │  Service  │ │  Service   │ │  Service  │
       ├───────────┤ ├────────────┤ ├───────────┤
       │ MySQL     │ │ MySQL      │ │ MySQL     │
       └───────────┘ └────────────┘ └───────────┘
```

### 4.2 技术选型

| 组件 | 技术 | 用途 |
|------|------|------|
| 基础框架 | Spring Boot 3.2 | 应用容器 |
| ORM | Spring Data JPA + Hibernate | 持久化 |
| 数据库 | MySQL 8.0 | 数据存储 |
| 消息队列 | RabbitMQ | 异步领域事件 |
| 服务发现 | Netflix Eureka | 服务注册与发现 |
| API 网关 | Spring Cloud Gateway | 统一入口/路由 |
| 构建 | Maven 多模块 | 项目管理 |

### 4.3 Maven 模块结构

```
ddd-demo (parent pom)
├── common                    # 共享抽象
│   ├── domain/               # AggregateRoot, DomainEvent 基类
│   └── exception/            # DomainException
├── order-service             # 订单服务 (8081)
├── product-service           # 商品服务 (8082)
├── payment-service           # 支付服务 (8083)
├── gateway-service           # 网关服务 (8080)
└── discovery-service         # 注册中心 (8761)
```

### 4.4 数据库设计

每个服务独立 Schema，表名与限界上下文术语一致：

**order-service (`ddd_order`)**

```sql
CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE order_items (
    order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL
);
```

**product-service (`ddd_product`)**

```sql
CREATE TABLE products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    available_quantity INT NOT NULL,
    reserved_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    category_id BIGINT,
    created_at TIMESTAMP
);
```

**payment-service (`ddd_payment`)**

```sql
CREATE TABLE payments (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    method VARCHAR(20),
    created_at TIMESTAMP
);

CREATE TABLE transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,  -- PAYMENT, REFUND
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP
);
```

---

## 五、API 设计

### order-service

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/{id}` | 查询订单 |
| GET | `/api/orders` | 订单列表 |
| POST | `/api/orders/{id}/cancel` | 取消订单 |
| POST | `/api/orders/{id}/pay` | 支付订单（触发 Payment Service） |

### product-service

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/products/{id}` | 查询商品 |
| GET | `/api/products` | 商品列表 |

### payment-service

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/payments` | 发起支付 |
| GET | `/api/payments/{id}` | 查询支付 |
| POST | `/api/payments/{id}/refund` | 退款 |

---

## 六、错误处理

### 领域异常层次

```
DomainException (RuntimeException)
├── OrderValidationException      # 订单校验失败
├── InsufficientStockException    # 库存不足
├── InvalidStatusException        # 状态转换非法
├── PaymentFailedException        # 支付失败
└── BusinessRuleViolation         # 通用业务规则违反
```

### 错误响应格式

```json
{
    "code": "ORDER_STATUS_INVALID",
    "message": "只能取消待处理的订单",
    "detail": "当前订单状态: PAID",
    "timestamp": "2026-05-17T10:30:00Z"
}
```

---

## 七、DDD 概念教学索引

以下概念在代码中均有对应实现，供学习参考：

| DDD 概念 | 对应代码 | 说明 |
|----------|---------|------|
| Entity | `Order`, `OrderItem`, `Product`, `Payment` | 有唯一标识，可变 |
| Value Object | `OrderId`, `Money`, `Address`, `Stock`, `OrderStatus` | 无标识，不可变，通过值比较 |
| Aggregate Root | `Order`, `Product`, `Payment` | 聚合入口，保证不变量 |
| Repository | `OrderRepository`, `ProductRepository`, `PaymentRepository` | 仓储接口在 Domain，实现在 Infrastructure |
| Domain Event | `OrderSubmittedEvent`, `PaymentSucceededEvent` | 过去发生的业务事实 |
| Domain Service | `OrderDomainService` | 跨多个实体的业务逻辑 |
| Application Service | `OrderApplicationService` | 流程编排，无业务逻辑 |
| Bounded Context | `order-service`, `product-service`, `payment-service` | 独立微服务 + 独立数据库 |
| Ubiquitous Language | 类名/方法名与业务术语一致 | 代码即业务文档 |
| Anti-Corruption Layer | Application 层的 Assembler/Translator | 防止外部模型污染领域模型 |
| Event-Driven | RabbitMQ 异步事件 | 保持限界上下文独立 |

---

## 八、项目路线图

| 阶段 | 内容 | 目标 |
|------|------|------|
| Phase 1 | 项目骨架搭建：父 POM、common 模块、各服务空模块 | 可启动的空项目 |
| Phase 2 | Domain 层实现：Entity、VO、Aggregate、Domain Event | 纯业务模型 |
| Phase 3 | Infrastructure 层：JPA 持久化、Repository 实现 | 数据可持久化 |
| Phase 4 | REST API 层实现 | 各服务可独立测试 |
| Phase 5 | 领域事件集成：RabbitMQ 配置、事件发布与消费 | 跨服务通讯 |
| Phase 6 | Gateway + Eureka 集成，完整端到端流程 | 全链路打通 |
| Phase 7 | 补充 DDD 教学注释和 README 文档 | 学习资料完善 |
