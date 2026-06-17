# Product Service Contract

**Service**: mall-product
**Internal Port**: 8083

## Consumer Endpoints

### GET /api/products
Paginated product listing.

**Query Params**: `page`, `size` (default 20), `category_id`, `sort` (price_asc/price_desc/sales/newest)

**Response** (200):
```json
{
  "code": 0,
  "data": {
    "items": [
      {
        "id": 1,
        "name": "string",
        "subtitle": "string",
        "price": 99.00,
        "original_price": 129.00,
        "image": "url (first image)",
        "sales_count": 1234,
        "merchant_name": "string"
      }
    ],
    "total": 5000,
    "page": 1,
    "size": 20
  }
}
```

### GET /api/products/{id}
Product detail.

**Response** (200):
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "name": "string",
    "subtitle": "string",
    "description": "rich text html",
    "price": 99.00,
    "original_price": 129.00,
    "stock": 500,
    "sales_count": 1234,
    "images": ["url1", "url2"],
    "category": {"id": 10, "name": "Electronics"},
    "merchant": {"id": 5, "shop_name": "string", "rating": 4.8},
    "related_products": [...]
  }
}
```

### GET /api/products/categories
Category tree.

**Response** (200):
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "icon": "url",
      "children": [
        {"id": 10, "name": "Phones", "children": [...]}
      ]
    }
  ]
}
```

## Merchant Endpoints

### POST /api/merchant/products
Create new product (status = pending_audit).

**Request**: multipart/form-data with product JSON + image files

### PUT /api/merchant/products/{id}
Update product info.

### PUT /api/merchant/products/{id}/offline
Take product offline.

### GET /api/merchant/products
List merchant's own products with status filter.
