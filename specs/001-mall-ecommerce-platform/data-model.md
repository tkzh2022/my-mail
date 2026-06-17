# Data Model: Mall E-Commerce Platform

**Date**: 2026-06-17

## Entity Relationship Overview

```
User (1) ──────── (N) Order
User (1) ──────── (N) Cart
User (1) ──────── (N) Address
User (1) ──────── (N) Notification
Merchant (1) ──── (N) Product
Product (N) ────── (1) Category
Category (self) ── parent/child hierarchy
Order (1) ──────── (N) OrderItem
Order (1) ──────── (0..1) Payment
Order (1) ──────── (0..1) Refund
Product (1) ────── (0..1) SeckillProduct
Admin (1) ──────── (N) AuditLog
```

## Entities

### User

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| username | VARCHAR(50) | UNIQUE, NOT NULL | |
| email | VARCHAR(100) | UNIQUE, NOT NULL | |
| phone | VARCHAR(20) | UNIQUE, NOT NULL | |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt encoded |
| nickname | VARCHAR(50) | | Display name |
| avatar_url | VARCHAR(500) | | |
| status | TINYINT | NOT NULL, DEFAULT 1 | 0=disabled, 1=active |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**State transitions**: active ↔ disabled (by admin)

### Merchant

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| user_id | BIGINT | FK → User, UNIQUE | One-to-one with User |
| shop_name | VARCHAR(100) | NOT NULL | |
| business_license | VARCHAR(50) | UNIQUE, NOT NULL | |
| contact_name | VARCHAR(50) | NOT NULL | |
| contact_phone | VARCHAR(20) | NOT NULL | |
| description | TEXT | | Shop description |
| logo_url | VARCHAR(500) | | |
| status | TINYINT | NOT NULL, DEFAULT 0 | 0=pending, 1=approved, 2=rejected, 3=suspended |
| rating | DECIMAL(3,2) | DEFAULT 5.00 | Aggregated rating |
| reject_reason | VARCHAR(500) | | If rejected |
| approved_at | DATETIME | | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**State transitions**: pending → approved / rejected; approved → suspended → approved

### Admin

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| username | VARCHAR(50) | UNIQUE, NOT NULL | |
| password_hash | VARCHAR(255) | NOT NULL | |
| real_name | VARCHAR(50) | NOT NULL | |
| role | VARCHAR(20) | NOT NULL | super_admin / operator |
| permissions | JSON | | Granular permission list |
| status | TINYINT | NOT NULL, DEFAULT 1 | 0=disabled, 1=active |
| last_login_at | DATETIME | | |
| created_at | DATETIME | NOT NULL | |

### Category

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| name | VARCHAR(50) | NOT NULL | |
| parent_id | BIGINT | FK → Category, NULL for root | |
| level | TINYINT | NOT NULL | 1=root, 2=second, 3=leaf |
| sort_order | INT | DEFAULT 0 | |
| icon_url | VARCHAR(500) | | |
| status | TINYINT | NOT NULL, DEFAULT 1 | 0=hidden, 1=visible |
| created_at | DATETIME | NOT NULL | |

### Product

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| merchant_id | BIGINT | FK → Merchant, NOT NULL | |
| category_id | BIGINT | FK → Category, NOT NULL | Leaf category only |
| name | VARCHAR(200) | NOT NULL | |
| subtitle | VARCHAR(500) | | Brief description |
| description | TEXT | | Rich text / HTML |
| price | DECIMAL(10,2) | NOT NULL | Current selling price |
| original_price | DECIMAL(10,2) | | Strikethrough price |
| stock | INT | NOT NULL, DEFAULT 0 | Available quantity |
| sales_count | INT | DEFAULT 0 | Total sold |
| images | JSON | NOT NULL | Array of image URLs |
| status | TINYINT | NOT NULL, DEFAULT 0 | 0=pending_audit, 1=online, 2=offline, 3=rejected |
| reject_reason | VARCHAR(500) | | If rejected by admin |
| version | INT | DEFAULT 0 | Optimistic lock |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**State transitions**: pending_audit → online / rejected; online ↔ offline; offline → pending_audit (re-submit)

**Index strategy**: (category_id, status), (merchant_id, status), (name) for search sync

### Address

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| user_id | BIGINT | FK → User, NOT NULL | |
| receiver_name | VARCHAR(50) | NOT NULL | |
| receiver_phone | VARCHAR(20) | NOT NULL | |
| province | VARCHAR(20) | NOT NULL | |
| city | VARCHAR(20) | NOT NULL | |
| district | VARCHAR(20) | NOT NULL | |
| detail_address | VARCHAR(200) | NOT NULL | |
| is_default | TINYINT | DEFAULT 0 | |
| created_at | DATETIME | NOT NULL | |

### Cart

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| user_id | BIGINT | FK → User, NOT NULL | |
| product_id | BIGINT | FK → Product, NOT NULL | |
| quantity | INT | NOT NULL, DEFAULT 1 | |
| selected | TINYINT | DEFAULT 1 | 0=unselected, 1=selected |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**Unique constraint**: (user_id, product_id)

### Order

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| order_no | VARCHAR(32) | UNIQUE, NOT NULL | Business order number |
| user_id | BIGINT | FK → User, NOT NULL | |
| total_amount | DECIMAL(10,2) | NOT NULL | |
| pay_amount | DECIMAL(10,2) | NOT NULL | After discounts |
| freight_amount | DECIMAL(10,2) | DEFAULT 0 | |
| status | TINYINT | NOT NULL, DEFAULT 0 | See state machine below |
| payment_method | VARCHAR(20) | | alipay / wechat_pay |
| payment_time | DATETIME | | |
| shipping_time | DATETIME | | |
| delivery_time | DATETIME | | |
| complete_time | DATETIME | | |
| tracking_company | VARCHAR(50) | | |
| tracking_number | VARCHAR(50) | | |
| receiver_name | VARCHAR(50) | NOT NULL | Snapshot at order time |
| receiver_phone | VARCHAR(20) | NOT NULL | |
| receiver_address | VARCHAR(300) | NOT NULL | |
| remark | VARCHAR(500) | | |
| version | INT | DEFAULT 0 | Optimistic lock |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**State machine**:
```
created → paid → shipped → delivered → completed
created → cancelled (timeout/user cancel)
paid → refund_requested → refunding → refunded
delivered → refund_requested → refunding → refunded
```

**Index strategy**: (user_id, status), (order_no), (created_at)

### OrderItem

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| order_id | BIGINT | FK → Order, NOT NULL | |
| product_id | BIGINT | NOT NULL | |
| product_name | VARCHAR(200) | NOT NULL | Snapshot |
| product_image | VARCHAR(500) | NOT NULL | Snapshot |
| price | DECIMAL(10,2) | NOT NULL | Price at order time |
| quantity | INT | NOT NULL | |
| merchant_id | BIGINT | NOT NULL | |

### Payment

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| order_id | BIGINT | FK → Order, UNIQUE | |
| payment_no | VARCHAR(64) | UNIQUE, NOT NULL | Internal payment ID |
| trade_no | VARCHAR(64) | | Third-party transaction ID |
| payment_method | VARCHAR(20) | NOT NULL | alipay / wechat_pay |
| amount | DECIMAL(10,2) | NOT NULL | |
| status | TINYINT | NOT NULL, DEFAULT 0 | 0=pending, 1=success, 2=failed, 3=refunded |
| callback_content | TEXT | | Raw callback JSON |
| paid_at | DATETIME | | |
| created_at | DATETIME | NOT NULL | |

### Refund

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| refund_no | VARCHAR(32) | UNIQUE, NOT NULL | |
| order_id | BIGINT | FK → Order, NOT NULL | |
| user_id | BIGINT | FK → User, NOT NULL | |
| merchant_id | BIGINT | FK → Merchant, NOT NULL | |
| amount | DECIMAL(10,2) | NOT NULL | |
| reason | VARCHAR(500) | NOT NULL | |
| evidence_images | JSON | | Array of image URLs |
| status | TINYINT | NOT NULL, DEFAULT 0 | 0=requested, 1=merchant_approved, 2=merchant_rejected, 3=arbitrating, 4=completed, 5=cancelled |
| merchant_reply | VARCHAR(500) | | |
| admin_decision | VARCHAR(500) | | Arbitration result |
| admin_id | BIGINT | | FK → Admin, who arbitrated |
| completed_at | DATETIME | | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**State machine**:
```
requested → merchant_approved → completed (refund processed)
requested → merchant_rejected → arbitrating → completed/cancelled
requested → cancelled (user withdraws)
(auto-approve after 7 days no merchant response)
```

### SeckillProduct

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| product_id | BIGINT | FK → Product, UNIQUE | |
| seckill_price | DECIMAL(10,2) | NOT NULL | Flash-sale price |
| total_stock | INT | NOT NULL | Total seckill inventory |
| available_stock | INT | NOT NULL | Remaining (synced from Redis) |
| start_time | DATETIME | NOT NULL | Sale starts |
| end_time | DATETIME | NOT NULL | Sale ends |
| per_user_limit | INT | DEFAULT 1 | Max purchase per user |
| status | TINYINT | NOT NULL, DEFAULT 0 | 0=pending, 1=active, 2=ended |
| created_at | DATETIME | NOT NULL | |

### Notification

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| user_id | BIGINT | FK → User, NOT NULL | |
| type | VARCHAR(30) | NOT NULL | order_paid, order_shipped, refund_result, etc. |
| channel | VARCHAR(10) | NOT NULL | in_app / sms / email |
| title | VARCHAR(100) | NOT NULL | |
| content | TEXT | NOT NULL | |
| is_read | TINYINT | DEFAULT 0 | in_app only |
| sent_at | DATETIME | NOT NULL | |
| created_at | DATETIME | NOT NULL | |

**Index strategy**: (user_id, is_read, created_at)

### Recommendation

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| id | BIGINT | PK, auto-increment | |
| user_id | BIGINT | FK → User, NOT NULL | |
| product_id | BIGINT | FK → Product, NOT NULL | |
| score | DOUBLE | NOT NULL | Relevance score |
| algorithm_version | VARCHAR(20) | NOT NULL | |
| reason | VARCHAR(50) | | "similar_purchase", "category_popular", etc. |
| created_at | DATETIME | NOT NULL | |
| expires_at | DATETIME | NOT NULL | |

**Unique constraint**: (user_id, product_id, algorithm_version)

## Redis Data Structures

| Key Pattern | Type | Purpose |
|-------------|------|---------|
| `seckill:stock:{productId}` | String (integer) | Atomic inventory counter for flash-sale |
| `seckill:queue:{productId}` | List | Virtual queue of user IDs waiting |
| `seckill:purchased:{productId}` | Set | User IDs who already purchased (dedup) |
| `user:cart:{userId}` | Hash | Cart items for fast read (cache) |
| `product:hot` | Sorted Set | Trending products by view/purchase score |
| `search:suggest` | Sorted Set | Auto-complete suggestions by frequency |
| `user:session:{token}` | String (JSON) | JWT refresh token / session data |
| `product:detail:{productId}` | String (JSON) | Product detail cache (TTL: 5min) |
