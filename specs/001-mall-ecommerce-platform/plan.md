# Implementation Plan: Mall E-Commerce Platform

**Branch**: `001-mall-ecommerce-platform` | **Date**: 2026-06-17 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-mall-ecommerce-platform/spec.md`

## Summary

Full-stack e-commerce platform with SpringBoot/SpringCloud microservices backend and Vue 3 frontend. Core capabilities: user/merchant/admin authentication, product catalog with advanced search, high-QPS flash-sale ordering, payment integration (Alipay/WeChat Pay), refund management, personalized recommendations, and platform administration. Architecture optimized for 10,000+ QPS during flash-sale events using Redis pre-deduction, MQ async processing, and virtual queue mechanism.

## Technical Context

**Language/Version**: Java 17 (backend), TypeScript 5.x (frontend)

**Primary Dependencies**: SpringBoot 3.x, Spring Cloud Alibaba 2022.x, Vue 3.4+, Vite 5.x, Element Plus

**Storage**: MySQL 8.0 (primary), Redis 7.x (cache/queue/inventory), Elasticsearch 8.x (search), MinIO/OSS (images)

**Testing**: JUnit 5 + Mockito (unit), Spring Boot Test (integration), Cypress (E2E), JMeter (load)

**Target Platform**: Linux server (Docker/K8s), Web browser (responsive)

**Project Type**: Web application (microservice backend + SPA frontend)

**Performance Goals**: 10,000+ QPS for flash-sale endpoints, <200ms p95 for standard APIs, <1.5s page load

**Constraints**: <200ms p95 API response, <1.5s FCP, 99.9% availability normal / 99.5% flash-sale

**Scale/Scope**: 10,000+ concurrent users, millions of products, 7 user stories, ~15 microservices

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Security First | PASS | OAuth2/JWT auth, RBAC for 3 roles, input validation at gateway+service, encrypted sensitive data |
| II. High Availability & Performance | PASS | Redis pre-deduction, MQ async orders, Sentinel circuit breaker, CDN for static, read/write DB separation |
| III. User Experience First | PASS | Vue 3 SPA with code splitting, skeleton screens, WebSocket for queue updates, mobile-first responsive |
| IV. Microservice Governance | PASS | DDD bounded contexts, Nacos registry/config, Feign+MQ communication, Gateway for cross-cutting |
| V. Testability & Observability | PASS | JUnit+integration tests, Sleuth tracing, Prometheus metrics, structured JSON logging |
| VI. Data Consistency | PASS | Seata for order-payment-inventory, idempotency keys, Redis atomic DECR, daily reconciliation |

## Project Structure

### Documentation (this feature)

```text
specs/001-mall-ecommerce-platform/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── api-gateway.md
│   ├── user-service.md
│   ├── product-service.md
│   ├── order-service.md
│   ├── payment-service.md
│   └── search-service.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
backend/
├── mall-gateway/                # API Gateway (Spring Cloud Gateway)
├── mall-auth/                   # Authentication & Authorization service
├── mall-user/                   # User/Merchant/Admin management
├── mall-product/                # Product catalog & category management
├── mall-search/                 # Elasticsearch search service
├── mall-order/                  # Order lifecycle management
├── mall-cart/                   # Shopping cart service
├── mall-payment/                # Payment gateway integration
├── mall-seckill/                # Flash-sale/seckill service
├── mall-recommend/              # Recommendation engine
├── mall-notification/           # Notification service (in-app/SMS/email)
├── mall-admin/                  # Platform administration service
├── mall-common/                 # Shared utilities, DTOs, constants
└── mall-parent/                 # Maven parent POM

frontend/
├── src/
│   ├── views/
│   │   ├── home/               # Homepage with recommendations
│   │   ├── product/            # Product listing, detail, search
│   │   ├── cart/               # Shopping cart
│   │   ├── order/              # Order management
│   │   ├── seckill/            # Flash-sale pages
│   │   ├── user/               # User profile, addresses
│   │   ├── merchant/           # Merchant dashboard
│   │   └── admin/              # Admin panel
│   ├── components/             # Shared UI components
│   ├── composables/            # Vue composables (hooks)
│   ├── stores/                 # Pinia state management
│   ├── services/               # API client services
│   ├── router/                 # Vue Router configuration
│   └── utils/                  # Utilities
├── public/
└── tests/
    ├── unit/
    └── e2e/

infrastructure/
├── docker/                     # Dockerfiles for each service
├── docker-compose.yml          # Local development stack
├── k8s/                        # Kubernetes manifests
└── sql/                        # Database initialization scripts
```

**Structure Decision**: Web application with microservice backend (12 services + gateway + common module) and Vue 3 SPA frontend. Infrastructure-as-code with Docker Compose for local dev and K8s manifests for production.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| 12+ microservices | Each service aligns with a DDD bounded context (auth, user, product, order, cart, payment, seckill, search, recommend, notification, admin) | Monolith cannot meet 10K QPS flash-sale requirement; services must scale independently |
| Elasticsearch for search | Full-text with pinyin + fuzzy + synonym requires inverted index | MySQL LIKE queries cannot support fuzzy/pinyin/synonym search at scale |
| Separate seckill service | Flash-sale traffic patterns (burst 10K+ QPS) require isolated infrastructure to protect other services | Combining with order service risks cascade failure during flash sales |
