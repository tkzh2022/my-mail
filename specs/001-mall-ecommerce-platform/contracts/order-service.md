# Order Service Contract

**Service**: mall-order
**Internal Port**: 8085

## Endpoints

### POST /api/orders
Create order from cart items.

**Request**:
```json
{
  "address_id": 1,
  "cart_item_ids": [1, 2, 3],
  "remark": "string (optional)",
  "idempotency_key": "uuid"
}
```

**Response** (201):
```json
{
  "code": 0,
  "data": {
    "order_id": 789,
    "order_no": "202606171500001",
    "total_amount": 298.00,
    "pay_amount": 298.00,
    "payment_deadline": "2026-06-17T15:30:00Z"
  }
}
```

### GET /api/orders
List user's orders with status filter.

**Query Params**: `page`, `size`, `status` (all/unpaid/shipped/delivered/completed/refunding)

### GET /api/orders/{orderNo}
Order detail.

### PUT /api/orders/{orderNo}/cancel
Cancel unpaid order.

### PUT /api/orders/{orderNo}/confirm-delivery
User confirms delivery.

### POST /api/orders/{orderNo}/refund
Initiate refund request.

**Request**:
```json
{
  "reason": "string",
  "evidence_images": ["url1", "url2"]
}
```

## Merchant Endpoints

### GET /api/merchant/orders
List orders for merchant's products.

### PUT /api/merchant/orders/{orderNo}/ship
Update shipping info.

**Request**:
```json
{
  "tracking_company": "string",
  "tracking_number": "string"
}
```

### PUT /api/merchant/refunds/{refundNo}/approve
Approve refund.

### PUT /api/merchant/refunds/{refundNo}/reject
Reject refund with reason.

## Order State Machine

```
CREATED (0) → PAID (1) → SHIPPED (2) → DELIVERED (3) → COMPLETED (4)
CREATED (0) → CANCELLED (5)     [timeout 30min or user cancel]
PAID (1) → REFUND_REQUESTED (6) → REFUNDING (7) → REFUNDED (8)
DELIVERED (3) → REFUND_REQUESTED (6) → REFUNDING (7) → REFUNDED (8)
```

## Events Published (RocketMQ)

| Topic | Event | Trigger |
|-------|-------|---------|
| order-events | ORDER_CREATED | New order placed |
| order-events | ORDER_PAID | Payment confirmed |
| order-events | ORDER_SHIPPED | Merchant ships |
| order-events | ORDER_CANCELLED | Timeout or user cancel |
| refund-events | REFUND_REQUESTED | User initiates refund |
| refund-events | REFUND_COMPLETED | Refund processed |
