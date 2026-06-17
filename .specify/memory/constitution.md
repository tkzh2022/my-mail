<!--
## Sync Impact Report

- Version change: N/A (placeholder template) → 1.0.0
- Modified principles:
  - [PRINCIPLE_1_NAME] → I. Security First (安全第一)
  - [PRINCIPLE_2_NAME] → II. High Availability and Performance (高可用高性能)
  - [PRINCIPLE_3_NAME] → III. User Experience First (用户体验优先)
  - [PRINCIPLE_4_NAME] → IV. Microservice Governance (微服务治理)
  - [PRINCIPLE_5_NAME] → V. Testability and Observability (可测试与可观测)
  - (added) → VI. Data Consistency (数据一致性)
- Added sections:
  - Technology Stack
  - Development Workflow
  - Governance (filled)
- Removed sections: None
- Templates requiring updates:
  - .specify/templates/plan-template.md — ✅ aligned (Constitution Check section compatible)
  - .specify/templates/spec-template.md — ✅ aligned (Success Criteria supports performance metrics)
  - .specify/templates/tasks-template.md — ✅ aligned (Phase structure supports security/perf/test tasks)
- Follow-up TODOs: None
-->

# Mall E-Commerce Platform Constitution

## Core Principles

### I. Security First (安全第一)

All engineering decisions MUST prioritize security above convenience or speed-to-market.

- Zero tolerance for SQL injection, XSS, CSRF, and privilege escalation vulnerabilities
- All user inputs MUST be validated at both the API Gateway layer and the individual service layer
- Sensitive data (passwords, payment tokens, personal information) MUST be encrypted at rest (AES-256) and in transit (TLS 1.2+)
- Authentication MUST use OAuth2 + JWT; authorization MUST use RBAC with fine-grained permission controls
- All third-party dependencies MUST pass security audit before adoption; known CVEs MUST be patched within 48 hours
- Flash-sale endpoints MUST implement anti-bot measures (rate limiting, CAPTCHA, device fingerprinting)

### II. High Availability and Performance (高可用高性能)

The system MUST deliver sub-second responses under extreme concurrency, especially during flash-sale events.

- All user-facing APIs MUST respond within 200ms at p95 under normal load
- Flash-sale (秒杀) scenarios MUST support >= 10,000 QPS per service node via:
  - Redis-based inventory pre-deduction with Lua scripts for atomicity
  - Message queue (RocketMQ/Kafka) async order creation to decouple peak writes
  - Multi-level caching (Local Cache → Redis → DB) for hot product data
- Circuit breaker (Sentinel) MUST be configured for all downstream RPC calls with defined fallback strategies
- Service degradation and rate limiting strategies MUST be pre-defined and documented for each service
- Database read/write separation MUST be implemented; sharding strategy MUST be planned for tables exceeding 10M rows
- Static resources MUST be served via CDN with appropriate cache headers

### III. User Experience First (用户体验优先)

The frontend MUST deliver a smooth, responsive, and intuitive shopping experience across all devices.

- Frontend MUST achieve Lighthouse Performance score >= 90 on mobile
- All user interactions MUST provide instant visual feedback (skeleton screens, loading states, optimistic updates)
- Mobile-first responsive design; MUST support Chrome, Safari, Firefox (latest 2 versions) and WeChat WebView
- Error states MUST be user-friendly with actionable guidance (never expose raw stack traces or technical codes)
- Page initial load (FCP) MUST be < 1.5s; Time to Interactive (TTI) MUST be < 3s
- Flash-sale countdown and queue status MUST use WebSocket for real-time updates
- Vue components MUST implement code splitting and lazy loading for routes

### IV. Microservice Governance (微服务治理)

Each microservice MUST have clear ownership, well-defined boundaries, and standardized communication patterns.

- Each service MUST align with a DDD bounded context; cross-domain data access MUST go through published APIs
- Synchronous inter-service communication MUST use OpenFeign with retry and timeout policies
- Asynchronous communication MUST use RocketMQ or Kafka with guaranteed delivery and dead-letter queue handling
- Service registry and discovery MUST use Nacos; all configuration MUST be externalized via Nacos Config
- API versioning MUST follow URI path versioning (e.g., `/api/v1/`) for all public endpoints
- Gateway (Spring Cloud Gateway) MUST handle cross-cutting concerns: authentication, rate limiting, request logging, gray release routing
- Each service MUST own its database; direct cross-service database access is FORBIDDEN

### V. Testability and Observability (可测试与可观测)

The system MUST be observable in production and verifiable before deployment.

- Unit test coverage MUST be >= 70% for service layer logic; integration tests MUST cover all API contract endpoints
- Structured logging in JSON format with mandatory fields: traceId, spanId, timestamp, service, level, message
- Distributed tracing (Spring Cloud Sleuth + Zipkin/SkyWalking) MUST be enabled for all inter-service calls
- Metrics (Prometheus + Grafana) MUST track: QPS, p50/p95/p99 latency, error rate, JVM stats, Redis hit ratio
- Alerts MUST fire when: p95 latency > 500ms, error rate > 1%, Redis hit ratio < 90%, or JVM heap usage > 80%
- Load testing (JMeter/Gatling) MUST be performed before each release for flash-sale related services, simulating >= 2x expected peak QPS

### VI. Data Consistency (数据一致性)

Critical business flows MUST guarantee data correctness; non-critical flows MAY use eventual consistency.

- Distributed transactions for order-payment-inventory flows MUST use Seata (AT mode) or TCC pattern
- Eventual consistency is acceptable for non-critical flows (notifications, analytics, recommendation updates) via MQ + retry
- All write APIs MUST guarantee idempotency using unique request IDs or business idempotency keys
- Database operations MUST use optimistic locking (version field) for concurrent updates to prevent lost updates
- Flash-sale inventory MUST use Redis atomic decrement (DECR/Lua) as the source of truth; DB sync MUST be asynchronous with reconciliation jobs
- Data reconciliation jobs MUST run daily to detect and alert on inconsistencies between Redis and DB

## Technology Stack

| Layer | Technology | Version/Notes |
|-------|-----------|---------------|
| Backend Framework | SpringBoot | 2.7.x / 3.x |
| Microservice Framework | Spring Cloud Alibaba | Latest stable |
| Frontend Framework | Vue 3 + Vite | Composition API, TypeScript |
| UI Component Library | Element Plus / Ant Design Vue | Responsive, accessible |
| Service Registry & Config | Nacos | Registry + Config Center |
| API Gateway | Spring Cloud Gateway | Routing, auth, rate limiting |
| Cache | Redis (Cluster mode) | Multi-level caching |
| Message Queue | RocketMQ / Kafka | Async decoupling, peak shaving |
| Database | MySQL 8.0 | Read/write separation, sharding-ready |
| Distributed Transaction | Seata | AT mode for critical flows |
| Rate Limiting & Circuit Breaker | Sentinel | Flow control, degradation |
| Distributed Tracing | Sleuth + Zipkin/SkyWalking | Full call chain visibility |
| Monitoring | Prometheus + Grafana | Metrics and alerting |
| Containerization | Docker + Kubernetes | Deployment and orchestration |
| CI/CD | Jenkins / GitLab CI | Automated pipeline |
| CDN | Cloud CDN | Static resource acceleration |

## Development Workflow

- Branching strategy: GitFlow (main, develop, feature/*, release/*, hotfix/*)
- All code changes MUST go through Pull Request with at least one reviewer approval
- CI pipeline MUST pass (compile, unit tests, lint, security scan) before merge is allowed
- Release branches MUST pass integration tests and load tests before production deployment
- Hotfixes MUST follow the same review process; emergency bypass requires post-facto review within 24 hours
- Database migrations MUST be backward-compatible and reviewed by DBA for tables with > 1M rows
- Feature flags MUST be used for gradual rollout of high-risk features (especially flash-sale logic changes)

## Governance

- This Constitution supersedes all conflicting local decisions, wiki pages, or verbal agreements
- Amendments require: (1) written proposal, (2) team lead approval, (3) version bump following semantic versioning, (4) migration plan for affected code
- All Pull Requests MUST verify compliance with applicable Constitution principles (reviewers MUST check)
- Complexity that violates any principle MUST be justified in writing in the PR description with explicit reasoning
- Quarterly review of Constitution principles to ensure alignment with evolving business and technical requirements
- Violations discovered in production MUST trigger a post-mortem and corrective action within 72 hours

**Version**: 1.0.0 | **Ratified**: 2026-06-17 | **Last Amended**: 2026-06-17
