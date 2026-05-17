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
| **防腐层** | 隔离不同上下文之间的模型差异 | `OrderAssembler` 在应用层做 DTO ↔ Domain 转换 |

### 2. 战术设计 (Tactical Design)

| 概念 | 说明 | 代码位置 |
|------|------|---------|
| **Entity** | 有唯一标识，可变 | `Order`, `OrderItem`, `Product`, `Payment`, `Transaction` |
| **Value Object** | 无标识，不可变，通过值比较 | `Money`, `Address`, `OrderId`, `Stock`, `OrderStatus` |
| **Aggregate Root** | 聚合入口，保证不变量 | `Order`, `Product`, `Payment` |
| **Domain Event** | 过去发生的业务事实 | `OrderSubmittedEvent`, `PaymentSucceededEvent` |
| **Repository** | 聚合的持久化抽象 | `OrderRepository`(接口) → `OrderRepositoryImpl`(JPA 实现) |
| **Application Service** | 流程编排，无业务逻辑 | `OrderApplicationService` |
| **Domain Service** | 跨多个实体的业务逻辑 | （本 Demo 未展示，推荐阅读参考资料） |

### 3. 六边形架构 (Hexagonal Architecture)

```
┌─────────────────────────────────────────┐
│            Interface (REST)             │
│         OrderController                 │
├─────────────────────────────────────────┤
│         Application Layer               │
│  OrderApplicationService                │
│  DTO / Assembler                        │
├─────────────────────────────────────────┤
│          Domain Layer                   │
│  Order / OrderItem / Money / ...        │
│  OrderRepository (interface)            │
├─────────────────────────────────────────┤
│        Infrastructure Layer             │
│  OrderRepositoryImpl (JPA)              │
│  OrderEventPublisher (RabbitMQ)         │
└─────────────────────────────────────────┘
```

**核心原则:** Domain 层零外部依赖，所有技术细节（JPA、MQ、REST）都在 Infrastructure 层。

### 4. 事件驱动架构

```
Order Service              Product Service             Payment Service
    │                           │                           │
    ├── submit() ─────────────► │                           │
    │   order.submitted         │ (reserve stock)           │
    │                           │                           │
    │                           │                           ├── listen: order.submitted
    │                           │                           │   → create & complete payment
    │◄──────────────────────────┤                           │
    │ payment.succeeded         │                           ├── payment.succeeded ──► │
    │   → markPaid()            │                           │
```

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
  -d '{"customerId":1,"street":"北京路1号","city":"上海","zipCode":"200000","items":[{"productId":1,"productName":"商品A","quantity":2,"unitPrice":{"amount":100.00,"currency":"CNY"}}]}'

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

## 参考资料

- [Domain-Driven Design Distilled (Vaughn Vernon)](https://www.amazon.com/Domain-Driven-Design-Distilled-Vaughn-Vernon/dp/0134434420)
- [Implementing Domain-Driven Design (Vaughn Vernon)](https://www.amazon.com/Implementing-Domain-Driven-Design-Vaughn-Vernon/dp/0321834577)
- [DDD 参考架构 (Microsoft)](https://learn.microsoft.com/en-us/dotnet/architecture/microservices/microservice-ddd-cqrs-patterns/)
