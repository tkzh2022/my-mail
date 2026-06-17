# API Gateway Contract

**Service**: mall-gateway
**Port**: 8080
**Base URL**: `/api`

## Routing Rules

| Path Pattern | Target Service | Auth Required | Rate Limit |
|-------------|----------------|---------------|------------|
| `/api/auth/**` | mall-auth | No | 100/min per IP |
| `/api/users/**` | mall-user | Yes | 300/min per user |
| `/api/products/**` | mall-product | Mixed | 600/min per user |
| `/api/search/**` | mall-search | No | 300/min per user |
| `/api/cart/**` | mall-cart | Yes | 300/min per user |
| `/api/orders/**` | mall-order | Yes | 200/min per user |
| `/api/payment/**` | mall-payment | Yes | 50/min per user |
| `/api/seckill/**` | mall-seckill | Yes | 10/sec per user |
| `/api/recommend/**` | mall-recommend | Mixed | 300/min per user |
| `/api/notifications/**` | mall-notification | Yes | 200/min per user |
| `/api/merchant/**` | mall-product | Yes (Merchant) | 200/min |
| `/api/admin/**` | mall-admin | Yes (Admin) | 300/min |

## Cross-Cutting Concerns

- JWT token validation (except /auth/** endpoints)
- Request ID generation (X-Request-Id header)
- Access logging (request path, duration, status)
- CORS configuration (frontend domain whitelist)
- Response compression (gzip for >1KB)
- Global error response format:

```json
{
  "code": 40001,
  "message": "Human-readable error message",
  "timestamp": "2026-06-17T15:00:00Z",
  "requestId": "uuid"
}
```
