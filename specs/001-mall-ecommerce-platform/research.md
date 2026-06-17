# Research: Mall E-Commerce Platform

**Date**: 2026-06-17

## Decision Log

### 1. Flash-Sale Architecture Pattern

**Decision**: Redis Lua script atomic decrement + RocketMQ async order creation + virtual queue via WebSocket

**Rationale**: This is the proven pattern used by Taobao/JD for seckill scenarios. Redis Lua guarantees atomic inventory check-and-decrement in a single round-trip (~1ms). RocketMQ decouples the hot path (inventory check) from the cold path (order persistence), allowing the system to absorb 10K+ QPS bursts without DB pressure. WebSocket-based virtual queue provides real-time position updates without polling overhead.

**Alternatives considered**:
- Database optimistic locking: Rejected — MySQL cannot sustain 10K+ QPS on a single row with acceptable latency
- Distributed lock (Redisson): Rejected — serializes all requests, throughput limited to lock acquire/release speed (~1K/s)
- Message queue only (no Redis): Rejected — cannot provide instant feedback on success/failure; queue backlog causes user confusion

### 2. Search Engine Selection

**Decision**: Elasticsearch 8.x with IK analyzer (Chinese word segmentation) + pinyin plugin + synonym filter

**Rationale**: ES provides native inverted index for full-text search, built-in relevance scoring (BM25), and plugin ecosystem for Chinese NLP. IK analyzer handles Chinese segmentation; pinyin plugin supports "shouji" → "手机" mapping; synonym filter handles alternate terms. Auto-complete via completion suggester with context-aware boosting.

**Alternatives considered**:
- MySQL FULLTEXT index: Rejected — no pinyin support, poor Chinese segmentation, limited relevance tuning
- Apache Solr: Rejected — smaller ecosystem for Chinese plugins, heavier operational overhead
- Meilisearch: Rejected — less mature Chinese language support, fewer production references at scale

### 3. Payment Integration Strategy

**Decision**: Abstract payment gateway with strategy pattern; implement Alipay (official SDK) and WeChat Pay (V3 API) as first providers

**Rationale**: Strategy pattern allows adding new payment methods without modifying existing code. Both Alipay and WeChat Pay provide official Java SDKs with callback verification. Unified internal payment status model maps provider-specific states to system order states.

**Alternatives considered**:
- Third-party aggregated payment (Ping++, LianLianPay): Rejected — adds intermediary cost, reduces control over reconciliation, potential single point of failure
- Direct HTTP integration without SDK: Rejected — signature verification and callback security are complex; official SDKs reduce error risk

### 4. Recommendation Algorithm

**Decision**: Item-based collaborative filtering (offline batch) + real-time hot/trending (Redis sorted sets) for cold-start

**Rationale**: Item-CF is well-proven for e-commerce, scales linearly with product catalog size, and can be computed as batch jobs (Spark/Flink). Redis sorted sets maintain real-time popularity rankings for cold-start users. User behavioral signals (view, add-to-cart, purchase) weighted differently for relevance scoring.

**Alternatives considered**:
- User-based CF: Rejected — does not scale well with growing user base; sparse matrix at early stage
- Deep learning (embedding-based): Rejected — overkill for v1; requires GPU infrastructure and significant training data
- Rule-based only: Rejected — cannot personalize beyond "same category" suggestions

### 5. Notification Delivery Architecture

**Decision**: Event-driven with RocketMQ topic per notification type; separate consumers for in-app (WebSocket push), SMS (third-party gateway), and email (transactional email service)

**Rationale**: Decoupling notification dispatch from business logic via MQ ensures order/payment services are not blocked by slow SMS/email delivery. Per-channel consumers allow independent scaling (SMS has rate limits, email has warm-up requirements). Failed deliveries are retried via dead-letter queue with exponential backoff.

**Alternatives considered**:
- Synchronous notification in business flow: Rejected — SMS/email latency (500ms-3s) would violate p95 <200ms requirement
- Single notification consumer handling all channels: Rejected — one slow channel blocks others; no independent scaling
- Third-party notification orchestration (e.g., Firebase): Rejected — adds external dependency for core business flow; data sovereignty concerns in China market

### 6. Database Sharding Strategy

**Decision**: Initially single-instance MySQL per service (database-per-service); prepare for ShardingSphere-JDBC sharding on order and product tables when approaching 10M rows

**Rationale**: Premature sharding adds complexity without benefit at launch scale. Database-per-service (constitution requirement) provides natural isolation. ShardingSphere-JDBC is application-transparent and requires minimal code changes when scaling triggers are met. Sharding key: user_id for orders, category_id for products.

**Alternatives considered**:
- Immediate sharding from day one: Rejected — YAGNI; adds 2-3 weeks development time, complicates local development
- Vitess/TiDB: Rejected — operational complexity and cloud cost exceeds budget for initial launch
- Manual application-level routing: Rejected — error-prone, requires significant code change vs ShardingSphere's declarative config

### 7. Frontend State Management

**Decision**: Pinia for global state (auth, cart, user profile) + Vue composables for component-local state + TanStack Query for server state caching

**Rationale**: Pinia is the official Vue 3 state management, lightweight and TypeScript-native. TanStack Query handles API response caching, background refetching, and optimistic updates — critical for the "instant feedback" UX requirement. Composables encapsulate reusable logic without global state overhead.

**Alternatives considered**:
- Vuex 4: Rejected — deprecated in favor of Pinia; more boilerplate, weaker TypeScript support
- Pinia only (no TanStack Query): Rejected — would require manual cache invalidation and loading state management for every API call
- Redux-style (via reactive stores): Rejected — over-engineered for Vue ecosystem; breaks convention
