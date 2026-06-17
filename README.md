# Mall E-Commerce Platform

基于 Spring Cloud Alibaba 的微服务电商平台，支持用户/商户/管理员三种角色，涵盖商品浏览、购物车、订单管理、支付、秒杀抢购、智能推荐等完整电商场景。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Java 17, Spring Boot 3.2.5, Spring Cloud Alibaba 2022.x |
| 服务治理 | Nacos (注册/配置中心), Feign (服务调用), Spring Cloud Gateway |
| 数据存储 | MySQL 8.0, Redis 7.x, Elasticsearch 8.x |
| 消息队列 | RocketMQ 5.x |
| 分布式事务 | Seata |
| 前端 | Vue 3.4+, TypeScript, Vite 5.x, Element Plus |
| 测试 | JUnit 5, Mockito, Spring Boot Test, JaCoCo |
| 部署 | Docker, Docker Compose, Kubernetes |

## 项目结构

```
backend/
├── mall-parent/         # Maven 父 POM (依赖版本管理)
├── mall-common/         # 公共模块 (异常、工具类、DTO)
├── mall-gateway/        # API 网关
├── mall-auth/           # 认证授权 (JWT)
├── mall-user/           # 用户/商户管理
├── mall-product/        # 商品目录、分类管理
├── mall-search/         # Elasticsearch 搜索
├── mall-order/          # 订单生命周期管理
├── mall-cart/           # 购物车
├── mall-payment/        # 支付集成
├── mall-seckill/        # 秒杀服务 (Redis Lua 原子扣减)
├── mall-recommend/      # 个性化推荐
├── mall-notification/   # 消息通知
└── mall-admin/          # 平台管理后台

frontend/                # Vue 3 SPA 前端

infrastructure/
├── docker-compose.yml   # 本地开发环境编排
├── k8s/                 # Kubernetes 部署清单
└── sql/                 # 数据库初始化脚本
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (前端开发)

### 1. 启动基础设施

```bash
cd infrastructure
docker-compose up -d
```

启动 MySQL、Redis、Nacos、RocketMQ、Elasticsearch、Seata 等基础服务。

### 2. 初始化数据库

数据库初始化脚本位于 `infrastructure/sql/` 目录，Docker Compose 会自动执行。

### 3. 编译后端

```bash
cd backend/mall-parent
mvn clean package -DskipTests
```

### 4. 启动服务

按以下顺序启动各微服务：

1. mall-gateway (端口 8080)
2. mall-auth (端口 8081)
3. mall-user (端口 8082)
4. mall-product (端口 8083)
5. mall-order (端口 8084)
6. mall-cart (端口 8087)
7. mall-payment (端口 8086)
8. mall-seckill (端口 8085)
9. mall-search (端口 8088)
10. mall-recommend (端口 8089)
11. mall-notification (端口 8090)
12. mall-admin (端口 8091)

### 5. 运行测试

```bash
cd backend/mall-parent
mvn clean test
```

当前测试覆盖：
- **167 个测试**全部通过
- 单元测试：覆盖全部 Service 层 (JUnit 5 + Mockito)
- Controller 测试：MockMvc 验证 HTTP 接口
- 集成测试：@SpringBootTest + H2 内存数据库验证数据流

### 6. 生成覆盖率报告

```bash
cd backend/mall-parent
mvn clean test jacoco:report
```

报告生成于各模块 `target/site/jacoco/index.html`。

## 核心架构

### 秒杀方案

采用 Redis Lua 脚本原子扣减库存 + RocketMQ 异步创建订单的方案，支持 10,000+ QPS：

1. Redis 预热库存
2. Lua 脚本原子判断+扣减
3. MQ 消息异步下单
4. 虚拟队列机制防止超卖

### 认证方案

JWT Token + Refresh Token 双令牌机制：
- Access Token: 短有效期 (30min)
- Refresh Token: 长有效期 (7天), 支持无感续签

### 分布式事务

使用 Seata AT 模式保证订单-支付-库存的数据一致性。

## API 文档

详细 API 契约文档位于 `specs/001-mall-ecommerce-platform/contracts/` 目录。

## 开发规范

- 统一异常处理：`BizException` + 错误码
- 统一响应格式：`R<T>` 封装
- 分页封装：`PageResult<T>`
- ID 生成：雪花算法变体 (`IdGenerator`)

## License

Internal Project - All Rights Reserved
