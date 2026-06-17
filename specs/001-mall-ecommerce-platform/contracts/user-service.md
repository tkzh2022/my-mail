# User Service Contract

**Service**: mall-auth + mall-user
**Internal Port**: 8081 (auth), 8082 (user)

## Authentication Endpoints (mall-auth)

### POST /api/auth/register
Register a new user account.

**Request**:
```json
{
  "username": "string (3-50 chars)",
  "email": "string (valid email)",
  "phone": "string (11 digits, China mobile)",
  "password": "string (8-32 chars, mixed case + number)",
  "verification_code": "string (6 digits)"
}
```

**Response** (201):
```json
{
  "code": 0,
  "data": {
    "user_id": 123,
    "username": "string",
    "token": "jwt-access-token",
    "refresh_token": "jwt-refresh-token"
  }
}
```

### POST /api/auth/login
Authenticate user with credentials.

**Request**:
```json
{
  "account": "string (username/email/phone)",
  "password": "string",
  "login_type": "password | sms_code"
}
```

**Response** (200):
```json
{
  "code": 0,
  "data": {
    "user_id": 123,
    "username": "string",
    "role": "user | merchant | admin",
    "token": "jwt-access-token",
    "refresh_token": "jwt-refresh-token",
    "expires_in": 7200
  }
}
```

### POST /api/auth/refresh
Refresh access token.

### POST /api/auth/logout
Invalidate current session.

## User Endpoints (mall-user)

### GET /api/users/profile
Get current user profile.

### PUT /api/users/profile
Update user profile (nickname, avatar).

### GET /api/users/addresses
List user addresses.

### POST /api/users/addresses
Add a new address.

### PUT /api/users/addresses/{id}
Update an address.

### DELETE /api/users/addresses/{id}
Delete an address.

## Merchant Registration (mall-user)

### POST /api/merchant/register
Submit merchant application.

**Request**:
```json
{
  "shop_name": "string",
  "business_license": "string",
  "contact_name": "string",
  "contact_phone": "string",
  "description": "string"
}
```

**Response** (201):
```json
{
  "code": 0,
  "data": {
    "merchant_id": 456,
    "status": "pending"
  }
}
```
