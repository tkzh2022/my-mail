# Payment Service Contract

**Service**: mall-payment
**Internal Port**: 8086

## Endpoints

### POST /api/payment/create
Create payment for an order.

**Request**:
```json
{
  "order_no": "string",
  "payment_method": "alipay | wechat_pay",
  "return_url": "string (frontend callback URL)"
}
```

**Response** (200):
```json
{
  "code": 0,
  "data": {
    "payment_no": "string",
    "pay_url": "string (redirect URL for Alipay / QR code data for WeChat)",
    "expires_in": 1800
  }
}
```

### GET /api/payment/{paymentNo}/status
Query payment status (for polling).

### POST /api/payment/callback/alipay
Alipay async notification callback (server-to-server).

### POST /api/payment/callback/wechat
WeChat Pay async notification callback (server-to-server).

### POST /api/payment/refund
Process refund to original payment method.

**Request** (internal, from order-service via Feign):
```json
{
  "order_no": "string",
  "refund_no": "string",
  "amount": 99.00,
  "reason": "string"
}
```

## Idempotency

- All payment creation requests use `order_no` as natural idempotency key
- Duplicate requests return the existing payment record
- Callback processing uses `trade_no` to prevent double-processing

## Reconciliation

- Daily reconciliation job at 01:00 compares internal records with provider settlement files
- Discrepancies trigger alerts via Hubble monitoring
