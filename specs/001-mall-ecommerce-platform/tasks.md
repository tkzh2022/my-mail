# Tasks: Mall E-Commerce Platform

**Input**: Design documents from `specs/001-mall-ecommerce-platform/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Tests are NOT explicitly requested. Test tasks excluded.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/mall-{service}/src/main/java/com/mall/{module}/`
- **Frontend**: `frontend/src/`
- **Infrastructure**: `infrastructure/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, Maven parent POM, Docker Compose, and common module

- [x] T001 Create root project directory structure per implementation plan (backend/, frontend/, infrastructure/)
- [x] T002 Create Maven parent POM with SpringBoot 3.x and Spring Cloud Alibaba dependency management in backend/mall-parent/pom.xml
- [x] T003 [P] Create mall-common module with shared DTOs, exception handling, and response wrapper in backend/mall-common/
- [x] T004 [P] Create Docker Compose file with MySQL, Redis, Elasticsearch, RocketMQ, Nacos, Zipkin in infrastructure/docker-compose.yml
- [x] T005 [P] Create database initialization SQL scripts (schema for all services) in infrastructure/sql/
- [x] T006 [P] Initialize Vue 3 + Vite + TypeScript project with Element Plus, Pinia, Vue Router, TanStack Query in frontend/
- [x] T007 [P] Configure frontend ESLint, Prettier, and Vite proxy settings in frontend/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T008 Create mall-gateway service with Spring Cloud Gateway, JWT filter, rate limiting, CORS, global error handling in backend/mall-gateway/
- [x] T009 [P] Create Nacos namespace and service configurations for all services in infrastructure/nacos/
- [x] T010 [P] Create mall-auth service skeleton with OAuth2/JWT token generation, refresh, and validation in backend/mall-auth/
- [x] T011 [P] Configure Sentinel flow control rules and fallback handlers in backend/mall-common/src/main/java/com/mall/common/sentinel/
- [x] T012 [P] Configure Sleuth + Zipkin distributed tracing in mall-common for all services in backend/mall-common/src/main/java/com/mall/common/trace/
- [x] T013 [P] Create shared Feign client configurations with retry and timeout policies in backend/mall-common/src/main/java/com/mall/common/feign/
- [x] T014 [P] Configure RocketMQ producer/consumer base classes in backend/mall-common/src/main/java/com/mall/common/mq/
- [x] T015 [P] Create frontend Axios HTTP client with JWT interceptor, error handling, and loading state in frontend/src/services/http.ts
- [x] T016 [P] Create frontend route configuration with auth guards and lazy loading in frontend/src/router/index.ts
- [x] T017 [P] Create frontend Pinia store for auth state (token, user info, role) in frontend/src/stores/auth.ts

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - User Registration and Authentication (Priority: P1) MVP

**Goal**: Users and merchants can register and authenticate with JWT-based session management

**Independent Test**: Register a user, login, verify JWT token is returned and can access protected resources

### Implementation for User Story 1

- [x] T018 [P] [US1] Create User entity and mapper in backend/mall-user/src/main/java/com/mall/user/entity/User.java
- [x] T019 [P] [US1] Create Merchant entity and mapper in backend/mall-user/src/main/java/com/mall/user/entity/Merchant.java
- [x] T020 [P] [US1] Create Address entity and mapper in backend/mall-user/src/main/java/com/mall/user/entity/Address.java
- [x] T021 [US1] Implement UserService with registration, profile CRUD in backend/mall-user/src/main/java/com/mall/user/service/UserService.java
- [x] T022 [US1] Implement MerchantService with registration (pending approval) in backend/mall-user/src/main/java/com/mall/user/service/MerchantService.java
- [x] T023 [US1] Implement AddressService with CRUD operations in backend/mall-user/src/main/java/com/mall/user/service/AddressService.java
- [x] T024 [US1] Implement AuthController (register, login, refresh, logout) in backend/mall-auth/src/main/java/com/mall/auth/controller/AuthController.java
- [x] T025 [US1] Implement UserController (profile, addresses) in backend/mall-user/src/main/java/com/mall/user/controller/UserController.java
- [x] T026 [US1] Implement SMS verification code sending and validation in backend/mall-auth/src/main/java/com/mall/auth/service/SmsService.java
- [x] T027 [P] [US1] Create frontend login page with form validation in frontend/src/views/user/LoginView.vue
- [x] T028 [P] [US1] Create frontend registration page (user + merchant tabs) in frontend/src/views/user/RegisterView.vue
- [x] T029 [P] [US1] Create frontend user profile page in frontend/src/views/user/ProfileView.vue
- [x] T030 [P] [US1] Create frontend address management component in frontend/src/views/user/AddressView.vue

**Checkpoint**: User Story 1 fully functional — register, login, manage profile and addresses

---

## Phase 4: User Story 2 - Product Browsing and Search (Priority: P1)

**Goal**: Users can browse products (paginated), filter by category, search with fuzzy/pinyin, view product details

**Independent Test**: Browse product list, apply category filter, search by keyword and pinyin, view product detail page

### Implementation for User Story 2

- [x] T031 [P] [US2] Create Product entity and mapper in backend/mall-product/src/main/java/com/mall/product/entity/Product.java
- [x] T032 [P] [US2] Create Category entity and mapper in backend/mall-product/src/main/java/com/mall/product/entity/Category.java
- [x] T033 [US2] Implement CategoryService with tree structure query in backend/mall-product/src/main/java/com/mall/product/service/CategoryService.java
- [x] T034 [US2] Implement ProductService with paginated listing, detail, related products in backend/mall-product/src/main/java/com/mall/product/service/ProductService.java
- [x] T035 [US2] Implement ProductController (list, detail, categories) in backend/mall-product/src/main/java/com/mall/product/controller/ProductController.java
- [x] T036 [US2] Create Elasticsearch index mapping with IK analyzer + pinyin plugin config in backend/mall-search/src/main/resources/es-mapping.json
- [x] T037 [US2] Implement SearchService with full-text search, fuzzy, pinyin, synonym, auto-complete in backend/mall-search/src/main/java/com/mall/search/service/SearchService.java
- [x] T038 [US2] Implement SearchController (search, suggest) in backend/mall-search/src/main/java/com/mall/search/controller/SearchController.java
- [x] T039 [US2] Implement product data sync listener (MQ consumer for product events → ES index) in backend/mall-search/src/main/java/com/mall/search/listener/ProductSyncListener.java
- [x] T040 [P] [US2] Create frontend homepage with product grid and infinite scroll in frontend/src/views/home/HomeView.vue
- [x] T041 [P] [US2] Create frontend category navigation component in frontend/src/components/CategoryNav.vue
- [x] T042 [P] [US2] Create frontend search bar with auto-complete dropdown in frontend/src/components/SearchBar.vue
- [x] T043 [P] [US2] Create frontend product list page with filters and sorting in frontend/src/views/product/ProductListView.vue
- [x] T044 [US2] Create frontend product detail page with images, description, related products in frontend/src/views/product/ProductDetailView.vue

**Checkpoint**: User Story 2 fully functional — browse, search (with pinyin), view details

---

## Phase 5: User Story 3 - Order Placement and Payment (Priority: P1)

**Goal**: Users can add to cart, place orders, and pay via Alipay/WeChat Pay with atomic inventory deduction

**Independent Test**: Add product to cart, create order, complete payment (sandbox), verify inventory decremented

### Implementation for User Story 3

- [x] T045 [P] [US3] Create Cart entity and mapper in backend/mall-cart/src/main/java/com/mall/cart/entity/Cart.java
- [x] T046 [P] [US3] Create Order and OrderItem entities in backend/mall-order/src/main/java/com/mall/order/entity/
- [x] T047 [P] [US3] Create Payment entity in backend/mall-payment/src/main/java/com/mall/payment/entity/Payment.java
- [x] T048 [US3] Implement CartService with Redis cache + DB persistence in backend/mall-cart/src/main/java/com/mall/cart/service/CartService.java
- [x] T049 [US3] Implement CartController (add, remove, update quantity, list) in backend/mall-cart/src/main/java/com/mall/cart/controller/CartController.java
- [x] T050 [US3] Implement OrderService with Seata distributed transaction (inventory deduction + order creation) in backend/mall-order/src/main/java/com/mall/order/service/OrderService.java
- [x] T051 [US3] Implement OrderController (create, list, detail, cancel, confirm delivery) in backend/mall-order/src/main/java/com/mall/order/controller/OrderController.java
- [x] T052 [US3] Implement PaymentService with Alipay SDK integration in backend/mall-payment/src/main/java/com/mall/payment/service/AlipayService.java
- [x] T053 [US3] Implement PaymentService with WeChat Pay V3 API integration in backend/mall-payment/src/main/java/com/mall/payment/service/WechatPayService.java
- [x] T054 [US3] Implement PaymentController (create, callback, status query) in backend/mall-payment/src/main/java/com/mall/payment/controller/PaymentController.java
- [x] T055 [US3] Implement order timeout auto-cancel scheduled task (30min) in backend/mall-order/src/main/java/com/mall/order/job/OrderTimeoutJob.java
- [x] T056 [US3] Implement order event publisher (MQ) for notification triggers in backend/mall-order/src/main/java/com/mall/order/mq/OrderEventPublisher.java
- [x] T057 [P] [US3] Create frontend shopping cart page in frontend/src/views/cart/CartView.vue
- [x] T058 [P] [US3] Create frontend checkout/order confirmation page in frontend/src/views/order/CheckoutView.vue
- [x] T059 [P] [US3] Create frontend payment page (QR code / redirect) in frontend/src/views/order/PaymentView.vue
- [x] T060 [P] [US3] Create frontend order list and detail pages in frontend/src/views/order/OrderListView.vue and OrderDetailView.vue
- [x] T061 [US3] Create frontend Pinia cart store with optimistic updates in frontend/src/stores/cart.ts

**Checkpoint**: User Story 3 fully functional — complete purchase flow with real payment integration

---

## Phase 6: User Story 4 - Merchant Product Management (Priority: P2)

**Goal**: Merchants can upload, edit, and take-offline products; products require admin audit before going live

**Independent Test**: Merchant uploads product with images, edits price, takes offline — verify status transitions

### Implementation for User Story 4

- [x] T062 [US4] Implement MerchantProductService (create with audit, update, offline, list own products) in backend/mall-product/src/main/java/com/mall/product/service/MerchantProductService.java
- [x] T063 [US4] Implement MerchantProductController in backend/mall-product/src/main/java/com/mall/product/controller/MerchantProductController.java
- [x] T064 [US4] Implement image upload to OSS/MinIO with validation and compression in backend/mall-product/src/main/java/com/mall/product/service/ImageUploadService.java
- [x] T065 [US4] Implement product event publisher (publish events for search sync) in backend/mall-product/src/main/java/com/mall/product/mq/ProductEventPublisher.java
- [x] T066 [P] [US4] Create frontend merchant dashboard layout in frontend/src/views/merchant/MerchantLayout.vue
- [x] T067 [P] [US4] Create frontend product upload form with image upload in frontend/src/views/merchant/ProductFormView.vue
- [x] T068 [P] [US4] Create frontend merchant product list with status badges in frontend/src/views/merchant/ProductListView.vue
- [x] T069 [US4] Create frontend merchant order management page in frontend/src/views/merchant/OrderManageView.vue

**Checkpoint**: User Story 4 fully functional — merchant can manage full product lifecycle

---

## Phase 7: User Story 5 - Personalized Recommendations (Priority: P3)

**Goal**: Homepage shows personalized product recommendations based on browsing/purchase history

**Independent Test**: User with browsing history sees personalized recommendations; new user sees trending products

### Implementation for User Story 5

- [x] T070 [P] [US5] Create Recommendation entity in backend/mall-recommend/src/main/java/com/mall/recommend/entity/Recommendation.java
- [x] T071 [US5] Implement user behavior tracking (view, add-to-cart, purchase events) in backend/mall-recommend/src/main/java/com/mall/recommend/service/BehaviorTrackingService.java
- [x] T072 [US5] Implement collaborative filtering recommendation algorithm in backend/mall-recommend/src/main/java/com/mall/recommend/service/RecommendService.java
- [x] T073 [US5] Implement trending/hot products via Redis sorted sets in backend/mall-recommend/src/main/java/com/mall/recommend/service/TrendingService.java
- [x] T074 [US5] Implement RecommendController (personalized, trending, cold-start) in backend/mall-recommend/src/main/java/com/mall/recommend/controller/RecommendController.java
- [x] T075 [US5] Create frontend recommendation carousel/grid component in frontend/src/components/RecommendSection.vue
- [x] T076 [US5] Integrate recommendations into homepage and product detail page in frontend/src/views/home/HomeView.vue

**Checkpoint**: User Story 5 fully functional — personalized and trending recommendations visible

---

## Phase 8: User Story 6 - Refund and Return Management (Priority: P2)

**Goal**: Users initiate refunds, merchants approve/reject, platform arbitrates disputes

**Independent Test**: User requests refund, merchant responds, verify state transitions and fund return

### Implementation for User Story 6

- [x] T077 [P] [US6] Create Refund entity and mapper in backend/mall-order/src/main/java/com/mall/order/entity/Refund.java
- [x] T078 [US6] Implement RefundService with state machine (request, approve, reject, arbitrate, auto-approve) in backend/mall-order/src/main/java/com/mall/order/service/RefundService.java
- [x] T079 [US6] Implement RefundController (user: create/cancel; merchant: approve/reject) in backend/mall-order/src/main/java/com/mall/order/controller/RefundController.java
- [x] T080 [US6] Implement refund auto-approve scheduler (7-day timeout) in backend/mall-order/src/main/java/com/mall/order/job/RefundAutoApproveJob.java
- [x] T081 [US6] Implement refund payment processing (call payment service to return funds) in backend/mall-payment/src/main/java/com/mall/payment/service/RefundPaymentService.java
- [x] T082 [P] [US6] Create frontend refund request form page in frontend/src/views/order/RefundRequestView.vue
- [x] T083 [P] [US6] Create frontend merchant refund management page in frontend/src/views/merchant/RefundManageView.vue

**Checkpoint**: User Story 6 fully functional — complete refund lifecycle with auto-approve

---

## Phase 9: User Story 7 - Platform Administration (Priority: P2)

**Goal**: Admins can approve merchants, audit products, arbitrate disputes, view analytics

**Independent Test**: Admin logs in, approves merchant, audits product, resolves dispute, views dashboard

### Implementation for User Story 7

- [x] T084 [P] [US7] Create Admin entity and mapper in backend/mall-admin/src/main/java/com/mall/admin/entity/Admin.java
- [x] T085 [US7] Implement AdminMerchantService (approve/reject merchant applications) in backend/mall-admin/src/main/java/com/mall/admin/service/MerchantAuditService.java
- [x] T086 [US7] Implement AdminProductService (audit/approve/reject products) in backend/mall-admin/src/main/java/com/mall/admin/service/ProductAuditService.java
- [x] T087 [US7] Implement AdminDisputeService (arbitrate refund disputes) in backend/mall-admin/src/main/java/com/mall/admin/service/DisputeService.java
- [x] T088 [US7] Implement AdminAnalyticsService (DAU, order volume, revenue, merchant stats) in backend/mall-admin/src/main/java/com/mall/admin/service/AnalyticsService.java
- [x] T089 [US7] Implement AdminController (merchants, products, disputes, analytics endpoints) in backend/mall-admin/src/main/java/com/mall/admin/controller/AdminController.java
- [x] T090 [P] [US7] Create frontend admin layout with sidebar navigation in frontend/src/views/admin/AdminLayout.vue
- [x] T091 [P] [US7] Create frontend merchant approval page in frontend/src/views/admin/MerchantApprovalView.vue
- [x] T092 [P] [US7] Create frontend product audit page in frontend/src/views/admin/ProductAuditView.vue
- [x] T093 [P] [US7] Create frontend dispute arbitration page in frontend/src/views/admin/DisputeView.vue
- [x] T094 [US7] Create frontend analytics dashboard with charts in frontend/src/views/admin/DashboardView.vue

**Checkpoint**: User Story 7 fully functional — admin panel with all management capabilities

---

## Phase 10: Flash-Sale (Seckill) System (Priority: P1 - Extension)

**Goal**: Support 10K+ QPS flash-sale with virtual queue, atomic inventory, and zero overselling

**Independent Test**: Configure seckill product, simulate 10K concurrent requests, verify zero overselling and queue experience

### Implementation for Flash-Sale

- [x] T095 [P] [US3] Create SeckillProduct entity and mapper in backend/mall-seckill/src/main/java/com/mall/seckill/entity/SeckillProduct.java
- [x] T096 [US3] Implement SeckillService with Redis Lua atomic decrement and queue management in backend/mall-seckill/src/main/java/com/mall/seckill/service/SeckillService.java
- [x] T097 [US3] Implement virtual queue management with WebSocket position broadcast in backend/mall-seckill/src/main/java/com/mall/seckill/service/QueueService.java
- [x] T098 [US3] Implement SeckillController (purchase entry, queue status, result query) in backend/mall-seckill/src/main/java/com/mall/seckill/controller/SeckillController.java
- [x] T099 [US3] Create Redis Lua script for atomic stock check-and-decrement in backend/mall-seckill/src/main/resources/lua/seckill.lua
- [x] T100 [US3] Implement seckill order MQ consumer (async order creation after queue success) in backend/mall-seckill/src/main/java/com/mall/seckill/mq/SeckillOrderConsumer.java
- [x] T101 [US3] Implement stock warm-up job (DB → Redis sync before sale start) in backend/mall-seckill/src/main/java/com/mall/seckill/job/StockWarmUpJob.java
- [x] T102 [P] [US3] Create frontend flash-sale listing page with countdown timers in frontend/src/views/seckill/SeckillListView.vue
- [x] T103 [US3] Create frontend flash-sale purchase page with WebSocket queue display in frontend/src/views/seckill/SeckillPurchaseView.vue

**Checkpoint**: Flash-sale system handles 10K+ QPS with zero overselling

---

## Phase 11: Notification System

**Purpose**: In-app + SMS + email notifications triggered by order/refund events

- [x] T104 [P] Implement NotificationService with channel routing (in-app, SMS, email) in backend/mall-notification/src/main/java/com/mall/notification/service/NotificationService.java
- [x] T105 [P] Implement SMS sender via third-party gateway in backend/mall-notification/src/main/java/com/mall/notification/sender/SmsSender.java
- [x] T106 [P] Implement email sender via transactional email service in backend/mall-notification/src/main/java/com/mall/notification/sender/EmailSender.java
- [x] T107 Implement notification MQ consumers (listen to order/refund events) in backend/mall-notification/src/main/java/com/mall/notification/listener/OrderNotificationListener.java
- [x] T108 Implement WebSocket push for in-app real-time notifications in backend/mall-notification/src/main/java/com/mall/notification/websocket/NotificationWebSocket.java
- [x] T109 [P] Create frontend notification bell icon with unread count in frontend/src/components/NotificationBell.vue
- [x] T110 Create frontend notification list page in frontend/src/views/user/NotificationView.vue

**Checkpoint**: All order state changes trigger appropriate notifications across channels

---

## Phase 12: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T111 [P] Configure Prometheus metrics export for all services in backend/mall-common/src/main/java/com/mall/common/metrics/
- [x] T112 [P] Create Grafana dashboard templates (QPS, latency, error rate, JVM) in infrastructure/grafana/
- [x] T113 [P] Create Kubernetes deployment manifests for all services in infrastructure/k8s/
- [x] T114 [P] Create Dockerfiles for all backend services in infrastructure/docker/
- [x] T115 Implement daily data reconciliation job (Redis ↔ DB stock sync) in backend/mall-seckill/src/main/java/com/mall/seckill/job/ReconciliationJob.java
- [x] T116 [P] Implement frontend skeleton screens and loading states for all pages in frontend/src/components/SkeletonLoader.vue
- [x] T117 [P] Implement frontend responsive design breakpoints for mobile in frontend/src/styles/responsive.scss
- [x] T118 Security hardening: API input validation, XSS filter, SQL injection prevention in backend/mall-gateway/
- [x] T119 Performance: configure multi-level cache (LocalCache → Redis → DB) for product detail in backend/mall-product/
- [x] T120 Run quickstart.md validation scenarios end-to-end

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-9)**: All depend on Foundational phase completion
  - US1 (Auth) should complete first as other stories need authenticated users
  - US2 (Products) can proceed after US1 (needs user context for search history)
  - US3 (Orders) depends on US2 (need products to order)
  - US4 (Merchant) can proceed in parallel with US3
  - US5 (Recommendations) depends on US2 (needs product data)
  - US6 (Refunds) depends on US3 (needs orders to refund)
  - US7 (Admin) can proceed after US1 (needs auth system)
- **Flash-Sale (Phase 10)**: Depends on US3 (order infrastructure)
- **Notifications (Phase 11)**: Depends on US3 (order events)
- **Polish (Phase 12)**: Depends on all desired user stories being complete

### Recommended Execution Order

```
Phase 1 → Phase 2 → US1 → US2 → US3 → Phase 10 → Phase 11
                                    ↘ US4 (parallel with US3)
                                    ↘ US7 (parallel with US2+)
                           US3 done → US6
                           US2 done → US5
                     All done → Phase 12
```

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel
- Within each user story, models ([P]) can be created in parallel
- Frontend views ([P]) within a story can be created in parallel
- US4 and US7 can run in parallel with US3
- Phase 11 notification senders ([P]) can be created in parallel

---

## Implementation Strategy

### MVP First (User Stories 1-3 + Flash-Sale)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US1 (Auth) — **STOP and VALIDATE**
4. Complete Phase 4: US2 (Products) — **STOP and VALIDATE**
5. Complete Phase 5: US3 (Orders + Payment) — **STOP and VALIDATE**
6. Complete Phase 10: Flash-Sale — **STOP and VALIDATE with load test**
7. Deploy MVP

### Incremental Delivery

- **v1.0**: Auth + Products + Orders + Flash-Sale = Core shopping experience
- **v1.1**: + Merchant Management + Admin Panel = Platform operations
- **v1.2**: + Recommendations + Notifications = Enhanced UX
- **v1.3**: + Refund Management = Complete after-sales
- **v2.0**: + Polish + Monitoring + K8s = Production-ready

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Flash-sale system (Phase 10) is critical for the "high QPS" requirement and should be prioritized after basic order flow
