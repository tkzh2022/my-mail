# Quickstart Validation Guide: Mall E-Commerce Platform

**Date**: 2026-06-17

## Prerequisites

- Java 17+ (JDK installed)
- Node.js 18+ and pnpm
- Docker and Docker Compose
- Maven 3.8+

## Infrastructure Setup

Start all dependencies via Docker Compose:

```bash
cd infrastructure
docker-compose up -d
```

This starts: MySQL 8.0, Redis 7.x, Elasticsearch 8.x, RocketMQ, Nacos, Zipkin

Verify infrastructure:
```bash
# Nacos console
curl http://localhost:8848/nacos/

# Redis ping
docker exec mall-redis redis-cli ping

# MySQL connection
docker exec mall-mysql mysql -uroot -proot -e "SELECT 1"

# Elasticsearch health
curl http://localhost:9200/_cluster/health
```

## Backend Services Startup

```bash
cd backend

# Build all modules
mvn clean package -DskipTests

# Start services in order (or use IDE):
# 1. Gateway
java -jar mall-gateway/target/mall-gateway.jar &

# 2. Auth + User
java -jar mall-auth/target/mall-auth.jar &
java -jar mall-user/target/mall-user.jar &

# 3. Product + Search
java -jar mall-product/target/mall-product.jar &
java -jar mall-search/target/mall-search.jar &

# 4. Cart + Order + Payment
java -jar mall-cart/target/mall-cart.jar &
java -jar mall-order/target/mall-order.jar &
java -jar mall-payment/target/mall-payment.jar &

# 5. Seckill + Recommend + Notification + Admin
java -jar mall-seckill/target/mall-seckill.jar &
java -jar mall-recommend/target/mall-recommend.jar &
java -jar mall-notification/target/mall-notification.jar &
java -jar mall-admin/target/mall-admin.jar &
```

Verify all services registered in Nacos: http://localhost:8848/nacos/#/serviceManagement

## Frontend Startup

```bash
cd frontend
pnpm install
pnpm dev
```

Access: http://localhost:5173

## Validation Scenarios

### Scenario 1: User Registration and Login

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phone":"13800138000","password":"Test1234!"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"testuser","password":"Test1234!","login_type":"password"}'

# Expected: JWT token returned
```

### Scenario 2: Product Browsing and Search

```bash
# List products (paginated)
curl http://localhost:8080/api/products?page=1&size=10

# Search with keyword
curl "http://localhost:8080/api/search/products?keyword=手机&page=1&size=10"

# Search with pinyin
curl "http://localhost:8080/api/search/products?keyword=shouji&page=1&size=10"

# Auto-complete
curl "http://localhost:8080/api/search/suggest?prefix=iph"

# Expected: Product lists with relevance ranking, highlight matches
```

### Scenario 3: Order Flow

```bash
TOKEN="Bearer <jwt-token-from-login>"

# Add to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"product_id":1,"quantity":2}'

# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"address_id":1,"cart_item_ids":[1],"idempotency_key":"uuid-1"}'

# Create payment
curl -X POST http://localhost:8080/api/payment/create \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"order_no":"202606171500001","payment_method":"alipay","return_url":"http://localhost:5173/payment/result"}'

# Expected: Order created, payment URL returned
```

### Scenario 4: Flash-Sale (Load Test)

```bash
# Use JMeter or script to simulate 10,000 concurrent requests:
# 1. Pre-load seckill product with stock=100 in Redis
# 2. Fire 10,000 concurrent POST /api/seckill/purchase requests
# 3. Verify: exactly 100 orders created, 9,900 "sold out" responses
# 4. Verify: no overselling (stock never goes negative)
```

### Scenario 5: Merchant Product Upload

```bash
MERCHANT_TOKEN="Bearer <merchant-jwt-token>"

# Upload product
curl -X POST http://localhost:8080/api/merchant/products \
  -H "Authorization: $MERCHANT_TOKEN" \
  -F "product={\"name\":\"Test Product\",\"category_id\":10,\"price\":99.00,\"stock\":100,\"description\":\"desc\"}" \
  -F "images=@product-image.jpg"

# Expected: Product created with status=pending_audit
```

## Expected Outcomes

| Scenario | Success Criteria |
|----------|-----------------|
| Registration/Login | JWT token returned, user created in DB |
| Product Browsing | Paginated results, <200ms response |
| Search | Relevant results with Chinese+pinyin support |
| Order Flow | Order+Payment created, inventory decremented |
| Flash-Sale | Zero overselling at 10K QPS |
| Merchant Upload | Product in pending_audit status |
| Refund | State transitions per contract |
| Notifications | In-app + SMS + email triggered on state change |

## Monitoring Validation

- Prometheus: http://localhost:9090 (service metrics)
- Grafana: http://localhost:3000 (dashboards)
- Zipkin: http://localhost:9411 (distributed tracing)
- Nacos: http://localhost:8848 (service registry)
