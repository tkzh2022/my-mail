# Search Service Contract

**Service**: mall-search
**Internal Port**: 8084

## Endpoints

### GET /api/search/products
Full-text product search.

**Query Params**:
- `keyword`: Search term (supports Chinese, pinyin, fuzzy)
- `category_id`: Filter by category (optional)
- `price_min`, `price_max`: Price range filter (optional)
- `sort`: relevance (default) / price_asc / price_desc / sales
- `page`, `size`: Pagination

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
        "image": "url",
        "sales_count": 1234,
        "highlight_name": "<em>string</em> with highlights"
      }
    ],
    "total": 150,
    "page": 1,
    "size": 20,
    "aggregations": {
      "categories": [{"id": 10, "name": "Phones", "count": 80}],
      "price_ranges": [{"range": "0-100", "count": 30}]
    }
  }
}
```

### GET /api/search/suggest
Auto-complete suggestions.

**Query Params**: `prefix` (2+ chars)

**Response** (200):
```json
{
  "code": 0,
  "data": ["iPhone 15", "iPhone case", "iPad Pro"]
}
```

### POST /api/search/sync (internal)
Sync product data to Elasticsearch index.

## Elasticsearch Index Design

**Index**: `mall_products`

**Analyzer chain**:
1. IK max-word analyzer (Chinese segmentation)
2. Pinyin filter (Chinese → pinyin mapping)
3. Synonym filter (configurable synonym dictionary)
4. Lowercase filter

**Fields mapping**:
- `name`: text (ik_max_word + pinyin + synonym)
- `subtitle`: text (ik_smart)
- `category_id`: keyword
- `price`: scaled_float
- `sales_count`: integer
- `status`: keyword
- `suggest`: completion (for auto-complete)

## Data Sync

- Product CRUD events published to `product-events` MQ topic
- Search service consumes events and updates ES index (near real-time, <3s delay)
- Full re-index scheduled weekly (Sunday 03:00)
