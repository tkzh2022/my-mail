# Feature Specification: Mall E-Commerce Platform

**Feature Branch**: `001-mall-ecommerce-platform`

**Created**: 2026-06-17

**Status**: Draft

**Input**: User description: "商城项目核心功能包括：1、用户注册、登录和商家注册、登录 2、用户分页浏览商品、分类搜索商品、查看商品详情、快速搜索相关商品、下单、付款 3、商家上传商品、下架商品、修改商品价格、详细信息等 4、根据用户信息推荐用户可能关注的商品"

## Clarifications

### Session 2026-06-17

- Q: Order state transition notifications — how are users notified? → A: In-app notifications + SMS for payment confirmation and shipping updates; email for order receipts
- Q: Flash-sale queue mechanism — what is the user-facing experience when demand exceeds supply? → A: Virtual queue with real-time position display and estimated wait time
- Q: Refund/return flow — who initiates and what is the dispute resolution path? → A: User-initiated with merchant approval required; platform arbitration for unresolved disputes
- Q: Product search capability scope? → A: Full-text search with fuzzy matching, pinyin support, synonym expansion, and popularity weighting
- Q: Platform admin role and capabilities? → A: Full admin panel with merchant approval, product/content audit, dispute arbitration, and basic analytics dashboard

## User Scenarios & Testing *(mandatory)*

### User Story 1 - User Registration and Authentication (Priority: P1)

Users (consumers) and merchants can register accounts and securely log in to access their respective dashboards and features.

**Why this priority**: Authentication is the foundation of all other features — no browsing, ordering, or product management can happen without identity.

**Independent Test**: Can be fully tested by registering a new user/merchant, logging in, and verifying access to their dashboard. Delivers value as a standalone identity system.

**Acceptance Scenarios**:

1. **Given** a new visitor, **When** they fill in registration form (username, email, password, phone) and submit, **Then** their account is created and they receive a verification message
2. **Given** a registered user, **When** they enter correct credentials, **Then** they are authenticated and redirected to their homepage
3. **Given** a merchant, **When** they register with business information (shop name, license number, contact), **Then** a merchant account is created pending platform admin approval
4. **Given** a logged-in user, **When** their session expires, **Then** they are prompted to re-authenticate without losing their current page context
5. **Given** a user who forgot their password, **When** they request a reset via email/phone, **Then** they can set a new password securely

---

### User Story 2 - Product Browsing and Search (Priority: P1)

Users can discover products through paginated browsing, category filtering, full-text keyword search (with fuzzy matching, pinyin support, and synonym expansion), and related product suggestions.

**Why this priority**: Product discovery is the core shopping experience — users must be able to find what they want to buy.

**Independent Test**: Can be tested by browsing product listings, applying category filters, searching by keyword (including pinyin and fuzzy terms), and viewing product details without requiring any purchase flow.

**Acceptance Scenarios**:

1. **Given** a user on the homepage, **When** they scroll through the product feed, **Then** products load in pages of configurable size with smooth infinite scroll
2. **Given** a user looking for a specific category, **When** they select a category from the navigation, **Then** only products in that category are displayed with proper pagination
3. **Given** a user searching by keyword, **When** they type in the search bar, **Then** relevant results appear ranked by relevance (with fuzzy matching, pinyin support, synonym expansion, and popularity weighting) with auto-complete suggestions
4. **Given** a user viewing a product detail page, **When** they view the "related products" section, **Then** they see similar or frequently-bought-together items
5. **Given** high concurrent traffic, **When** thousands of users browse simultaneously, **Then** page load time remains under 1.5 seconds

---

### User Story 3 - Order Placement and Payment (Priority: P1)

Users can add products to cart, place orders, and complete payment through a secure checkout flow. Users receive in-app and SMS notifications for payment confirmation and shipping updates, plus email receipts.

**Why this priority**: Order and payment is the revenue-generating core of the platform — without it, the mall has no business value.

**Independent Test**: Can be tested end-to-end by adding a product to cart, confirming order, and completing a payment (sandbox). Delivers the complete purchase experience.

**Acceptance Scenarios**:

1. **Given** a user viewing a product, **When** they click "Add to Cart" and specify quantity, **Then** the item is added to their shopping cart with correct price
2. **Given** a user with items in cart, **When** they proceed to checkout, **Then** they see order summary with itemized prices, shipping, and total
3. **Given** a user at checkout, **When** they select a payment method and confirm, **Then** payment is processed, order status changes to "paid", and user receives in-app notification + SMS confirmation
4. **Given** a flash-sale product with limited inventory, **When** multiple users attempt to purchase simultaneously, **Then** users enter a virtual queue with real-time position display; inventory is accurately decremented without overselling
5. **Given** a payment failure, **When** the transaction cannot be completed, **Then** the user is notified with a clear error and inventory is released
6. **Given** an order is shipped, **When** the merchant updates tracking info, **Then** user receives in-app notification + SMS with tracking details

---

### User Story 4 - Merchant Product Management (Priority: P2)

Merchants can upload new products, update product information (price, description, images), and remove products from the storefront. Products are subject to platform admin content audit before going live.

**Why this priority**: Merchants need to manage their inventory — but this is secondary to the consumer purchase flow since the platform needs products to function (can be seeded initially).

**Independent Test**: Can be tested by a merchant logging in, uploading a product with images/description/price, editing it, and then taking it offline. Verifiable without any consumer interaction.

**Acceptance Scenarios**:

1. **Given** a logged-in merchant, **When** they fill in product details (name, category, price, description, images, stock) and submit, **Then** the product enters "pending audit" state for platform admin review
2. **Given** a platform admin reviewing a product, **When** they approve it, **Then** the product becomes visible on the storefront
3. **Given** a merchant with existing products, **When** they update the price or description, **Then** changes are reflected immediately on the storefront (minor edits) or re-enter audit (significant changes)
4. **Given** a merchant wanting to remove a product, **When** they click "take offline", **Then** the product is no longer visible to consumers but data is preserved
5. **Given** a merchant uploading images, **When** they add product photos, **Then** images are validated (format, size), compressed, and displayed in the product gallery
6. **Given** bulk operations, **When** a merchant updates prices for multiple products, **Then** all changes are applied atomically or rolled back on failure

---

### User Story 5 - Personalized Product Recommendations (Priority: P3)

The system recommends products to users based on their browsing history, purchase history, and user profile characteristics.

**Why this priority**: Recommendations enhance user engagement and conversion rate but are not essential for the platform to function — the core browse/search/buy flow works without them.

**Independent Test**: Can be tested by observing recommended products change based on simulated user behavior (browsing/purchasing certain categories). Delivers personalization value.

**Acceptance Scenarios**:

1. **Given** a user with browsing history, **When** they visit the homepage, **Then** personalized recommendations appear based on their recent views
2. **Given** a user who purchased items in a category, **When** they browse products, **Then** related and complementary products are suggested
3. **Given** a new user with no history, **When** they visit the platform, **Then** popular and trending products are shown as default recommendations
4. **Given** the recommendation engine, **When** product data or user behavior changes, **Then** recommendations are updated within 6 hours (next batch cycle) or within 30 minutes for trending products via real-time score updates

---

### User Story 6 - Refund and Return Management (Priority: P2)

Users can initiate refund/return requests for eligible orders. Merchants review and approve/reject requests. Unresolved disputes are escalated to platform arbitration.

**Why this priority**: After-sales support is critical for user trust and platform credibility, and directly follows from the order/payment flow.

**Independent Test**: Can be tested by a user initiating a refund on a completed order, merchant responding, and verifying the refund state transition and fund return.

**Acceptance Scenarios**:

1. **Given** a user with a delivered order, **When** they initiate a return/refund request with reason and evidence, **Then** the request is sent to the merchant for review
2. **Given** a merchant receiving a refund request, **When** they approve it, **Then** the refund is processed and funds returned to the user's original payment method
3. **Given** a merchant rejecting a refund request, **When** the user disagrees, **Then** they can escalate to platform arbitration
4. **Given** a platform admin handling arbitration, **When** they review evidence from both parties, **Then** they issue a binding decision (approve or reject refund)
5. **Given** a refund request without merchant response, **When** 7 days pass, **Then** the request is auto-approved

---

### User Story 7 - Platform Administration (Priority: P2)

Platform administrators can approve merchants, audit product content, arbitrate disputes, and view basic analytics dashboard.

**Why this priority**: Admin capabilities are necessary to support merchant onboarding, content quality, and dispute resolution — all confirmed as platform requirements.

**Independent Test**: Can be tested by an admin logging in, approving a merchant, auditing a product, resolving a dispute, and viewing the analytics dashboard.

**Acceptance Scenarios**:

1. **Given** a platform admin, **When** they review a pending merchant application, **Then** they can approve or reject it with a reason
2. **Given** a platform admin, **When** they review a product pending audit, **Then** they can approve, reject (with reason), or request modifications
3. **Given** a platform admin handling a dispute, **When** they review both user and merchant evidence, **Then** they can issue a binding arbitration decision
4. **Given** a platform admin viewing analytics, **When** they access the dashboard, **Then** they see key metrics: daily active users (DAU), daily/monthly order volume, daily/monthly gross merchandise value (GMV), average order value, top merchants by revenue, refund rate, and conversion funnel (visit → cart → order → payment)

---

### Edge Cases

- What happens when a user attempts to purchase a product that becomes out-of-stock during checkout? → Order is rejected gracefully with notification and cart is updated
- How does the system handle concurrent flash-sale purchases exceeding inventory? → Users enter virtual queue with position display; atomic inventory decrement prevents overselling; excess requests after inventory exhaustion receive "sold out" response
- What happens when a merchant deletes a product that exists in users' carts? → Cart item is marked as unavailable; user is notified upon next cart view
- How does the system handle payment gateway timeout? → Transaction is marked as pending; reconciliation job resolves status within 5 minutes
- What happens when search returns zero results? → User sees helpful suggestions (alternative keywords, popular products, category browsing)
- What if a refund is initiated for an order paid via flash-sale discount? → Refund amount equals the actually paid amount (discounted price), not original price
- What if a merchant is deactivated while having active orders? → Existing orders are fulfilled; new orders blocked; admin notified for intervention
- What happens when a user leaves the flash-sale queue? → Queue position is held for 5 minutes; if not resumed, position is released and user must re-queue
- What happens in a multi-merchant cart when one merchant's item fails inventory check? → Only the failed item is removed; remaining items proceed to order; user is notified of the partial failure
- What happens when payment succeeds but order service is temporarily unavailable? → Payment service records success; reconciliation job detects orphan payments within 5 minutes and triggers order creation retry
- What happens when Elasticsearch is down? → System falls back to MySQL LIKE-based search with degraded results; users see a banner indicating limited search functionality
- What happens when duplicate payment callbacks arrive? → System uses trade_no as idempotency key; duplicate callbacks are acknowledged but not reprocessed
- What happens when a merchant edits a product while admin is auditing it? → Edit is queued; admin sees the version they started reviewing; after audit decision, pending edits are applied (if approved) or discarded (if rejected)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST support separate registration and authentication flows for consumers, merchants, and platform administrators with distinct role-based permissions
- **FR-002**: System MUST support paginated product listing with configurable page size (min 10, max 100, default 20 items per page)
- **FR-003**: System MUST support multi-level category-based product filtering with category tree navigation
- **FR-004**: System MUST support full-text keyword search with fuzzy matching, pinyin support, synonym expansion, popularity weighting, relevance ranking, and auto-complete suggestions
- **FR-005**: System MUST display product detail pages with images, description, price, stock status, and related products
- **FR-006**: System MUST support shopping cart operations (add, remove, update quantity) with persistent state across sessions; cart items persist indefinitely for authenticated users; unavailable products are marked but not auto-removed
- **FR-007**: System MUST process orders with atomic inventory deduction to prevent overselling under concurrent access
- **FR-008**: System MUST integrate with payment gateways supporting at least Alipay and WeChat Pay
- **FR-009**: System MUST allow merchants to create, update, and take-offline products with image upload support (max 10 images per product, max 5MB per image, JPEG/PNG/WebP formats, minimum 800x800px); new products require platform admin audit before going live; price changes >20% or category changes trigger re-audit; minor edits (description text, stock adjustment) take effect immediately
- **FR-010**: System MUST generate personalized product recommendations based on user behavior (browsing and purchase history)
- **FR-011**: System MUST support flash-sale scenarios with pre-announced time windows, countdown, virtual queue with real-time position display, and atomic inventory control; per-user purchase limit (default 1 item per flash-sale); second purchase attempt returns "limit exceeded" error; queue position is held for 5 minutes if user disconnects
- **FR-012**: System MUST maintain order lifecycle states (created, paid, shipped, delivered, completed, cancelled, refund-requested, refunding, refunded); unpaid orders MUST auto-cancel after 30 minutes
- **FR-013**: System MUST send in-app notifications for all order state changes, SMS for payment confirmation and shipping updates, and email for order receipts; failed notification deliveries MUST be retried up to 3 times with exponential backoff (1min, 5min, 30min); permanently failed notifications are logged for manual review
- **FR-014**: System MUST support user-initiated refund/return with merchant approval flow and platform arbitration for disputes
- **FR-015**: System MUST provide platform admin capabilities: merchant approval, product content audit, dispute arbitration, and basic analytics dashboard

### Key Entities

- **User**: Represents a consumer; key attributes: ID, username, email, phone, password hash, address book, registration date
- **Merchant**: Represents a seller; key attributes: ID, shop name, business license, contact info, approval status, rating
- **Admin**: Represents a platform administrator; key attributes: ID, username, role level, permissions, last login
- **Product**: Represents a saleable item; key attributes: ID, name, category, price, stock, description, images, merchant ID, status (pending-audit/online/offline/rejected), creation date
- **Category**: Hierarchical product classification; key attributes: ID, name, parent category ID, level, sort order
- **Order**: Represents a purchase transaction; key attributes: ID, user ID, order items, total amount, status, payment method, tracking info, timestamps
- **Cart**: Temporary holding of items before purchase; key attributes: user ID, product ID, quantity, selected status
- **Refund**: Represents a return/refund request; key attributes: ID, order ID, user ID, merchant ID, reason, evidence, status (requested/approved/rejected/arbitrating/completed), timestamps
- **Notification**: Represents a message to user; key attributes: ID, user ID, channel (in-app/SMS/email), type, content, read status, sent timestamp
- **Recommendation**: Computed suggestions; key attributes: user ID, product IDs, score, algorithm version, timestamp

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete the full purchase flow (browse → add to cart → checkout → pay) in under 3 minutes
- **SC-002**: Product search returns relevant results within 1 second for 95% of queries, including fuzzy and pinyin searches
- **SC-003**: System supports 10,000+ concurrent users browsing products without performance degradation
- **SC-004**: Flash-sale events handle 10,000+ simultaneous purchase attempts with zero overselling incidents; queue position updates within 2 seconds
- **SC-005**: Product pages load within 1.5 seconds on mobile networks (3G/4G)
- **SC-006**: Merchants can upload and publish a new product in under 5 minutes (excluding admin audit time)
- **SC-007**: Personalized recommendations achieve a click-through rate of at least 5% compared to non-personalized listings
- **SC-008**: System maintains 99.9% availability during normal operation and 99.5% during flash-sale events
- **SC-009**: 90% of users can successfully complete registration and first purchase on their first attempt without support
- **SC-010**: Refund requests receive merchant response within 48 hours; auto-approval triggers after 7 days of no response
- **SC-011**: Platform admin can process merchant approvals and product audits within 24 hours of submission
- **SC-012**: Refund funds are returned to user's original payment method within 3 business days of approval
- **SC-013**: Newly published products appear in search results within 3 seconds of admin approval
- **SC-014**: Merchant dashboard displays order data updated within 5 minutes of real-time

## Assumptions

- Users have stable internet connectivity (mobile 3G+ or broadband)
- The platform operates in the China market, supporting Alipay and WeChat Pay as primary payment methods
- Merchant approval is a manual process handled by platform administrators via the admin panel
- Product recommendation uses a collaborative filtering approach; ML model training is done offline periodically
- Mobile app support (iOS/Android native) is out of scope for v1 — the platform is web-based with responsive design
- The platform uses RMB (Chinese Yuan) as the sole currency in v1
- Image storage uses cloud object storage (e.g., OSS) — self-hosted storage is out of scope
- Flash-sale product quantity is pre-configured by merchants with administrator approval
- SMS delivery relies on a third-party SMS gateway provider
- Email delivery uses a standard transactional email service
- Platform admin accounts are pre-seeded by system deployment (no self-registration for admins)
- Internationalization is out of scope for v1 — platform operates exclusively in China with Chinese language UI
- Minimum user connectivity: 1Mbps download speed, 500ms maximum acceptable round-trip latency
- SMS gateway SLA: 99.5% delivery rate within 30 seconds; provider fallback configured for critical notifications
- Recommendation model retraining frequency: daily batch job at 02:00; real-time trending scores update every 30 minutes
- Data retention: orders and payment records retained for 3 years; notifications auto-archived after 90 days; user browsing history retained for 180 days
- Disaster recovery: RPO (Recovery Point Objective) ≤ 5 minutes; RTO (Recovery Time Objective) ≤ 30 minutes for critical services (auth, order, payment)
- Consumer role: can browse, search, cart, order, pay, refund, view recommendations
- Merchant role: all consumer abilities plus product management, order fulfillment, refund review
- Admin role: merchant approval, product audit, dispute arbitration, analytics; cannot place orders or manage products directly
