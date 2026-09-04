# Storefront API & Payload Structure (v1)

Customer-facing ecommerce API for the React Native mobile app.
Derived from the existing backend conventions in this repo (see "Backend Conventions" below).
Modules marked **[existing]** reuse current backend payloads verbatim; **[new]** are proposed additions for the storefront.

## Scope (v1)

1. User can browse products (categories, product list, search, product detail)
2. User can checkout and order

---

## Backend Conventions (extracted from this repo)

| Concern | Convention | Source |
|---|---|---|
| Base path | `/api/v1/{module}/...` | All controllers |
| Response style | Spring HATEOAS: single resource wrapped in `EntityModel` (adds `_links`), lists wrapped in `PagedModel` | `ProductController.java` |
| Naming | camelCase (Java records, default Jackson) | All DTOs |
| Pagination | `PagedModel`: `{ content, page: { size, number, totalElements, totalPages } }` + `_links` | Spring HATEOAS |
| Errors | RFC 7807 `ProblemDetail` via `GlobalApiExceptionHandler` | `store/shared/exception` |
| IDs | Opaque string identifiers (ULID-style), never integers | All DTOs |
| Money | `amount: number (BigDecimal)` + lowercase ISO `currencyCode` (e.g. `"mmk"`, `"usd"`) | `CurrencyCode.java`, `PriceResponse.java` |
| Auth | Bearer access token + refresh token, `AuthResponse` shape | `AuthController.java` |

### Pagination envelope (all list endpoints)

```json
{
  "content": [ ],
  "_links": {
    "first":   { "href": "/api/v1/storefront/products?page=0&size=20" },
    "self":    { "href": "/api/v1/storefront/products?page=0&size=20" },
    "next":    { "href": "/api/v1/storefront/products?page=1&size=20" },
    "last":    { "href": "/api/v1/storefront/products?page=4&size=20" }
  },
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 87,
    "totalPages": 5
  }
}
```

> Query params: `?page=0&size=20&sort=name,asc` (Spring `Pageable` defaults: `size=20`).

### Error envelope (RFC 7807 `ProblemDetail`)

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Product not found",
  "instance": "/api/v1/storefront/products/01J9F3...",
  "timestamp": "2026-08-25T10:15:30Z"
}
```

Validation error (400):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/storefront/cart/items",
  "timestamp": "2026-08-25T10:15:30Z",
  "errors": {
    "variantId": "must not be blank"
  }
}
```

---

# 1. Auth **[existing]**

Reuses `/api/v1/identity/auth/*` exactly as implemented (`AuthController`).

### 1.1 Register

`POST /api/v1/identity/auth/register`

```json
{ "email": "aung.aung@example.com", "password": "s3cret-Pass!" }
```

Response `200` → `UserProfileResponse`:

```json
{
  "id": "01J9F3K2M8N4P6Q8R0T2V4X6Z8",
  "email": "aung.aung@example.com",
  "status": "ACTIVE",
  "createdAt": "2026-08-25T10:15:30Z",
  "accessContexts": []
}
```

### 1.2 Login

`POST /api/v1/identity/auth/login`

```json
{ "email": "aung.aung@example.com", "password": "s3cret-Pass!" }
```

Response `200` → `AuthResponse`:

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.mock-access-token",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.mock-refresh-token",
  "expiresInMs": 900000,
  "userId": "01J9F3K2M8N4P6Q8R0T2V4X6Z8",
  "email": "aung.aung@example.com",
  "roles": ["CUSTOMER"],
  "status": "ACTIVE",
  "contextSelectionRequired": false
}
```

### 1.3 Refresh token

`POST /api/v1/identity/auth/refresh`

```json
{ "refreshToken": "eyJhbGciOiJIUzUxMiJ9.mock-refresh-token" }
```

Response `200` → same `AuthResponse` shape as login.

### 1.4 Logout

`POST /api/v1/identity/auth/logout`

```json
{ "refreshToken": "eyJhbGciOiJIUzUxMiJ9.mock-refresh-token" }
```

Response `204` no content.

### 1.5 Current profile **[existing]**

`GET /api/v1/identity/profile/me` → same shape as Register response.

All authenticated requests send:

```
Authorization: Bearer <accessToken>
```

---

# 2. Categories — Browse **[existing shape]**

Uses the catalog `CategoryNodeResponse` / `CategoryChildrenResponse` shapes.

### 2.1 Category tree (for home screen drawer / category tabs)

`GET /api/v1/storefront/categories/tree?rootId={categoryId}`

Response `200`:

```json
[
  {
    "id": "cat_electronics",
    "name": "Electronics",
    "parentId": null,
    "children": [
      {
        "id": "cat_mobile_phones",
        "name": "Mobile Phones",
        "parentId": "cat_electronics",
        "children": [
          {
            "id": "cat_smartphones",
            "name": "Smartphones",
            "parentId": "cat_mobile_phones",
            "children": []
          }
        ]
      },
      {
        "id": "cat_laptops",
        "name": "Laptops",
        "parentId": "cat_electronics",
        "children": []
      }
    ]
  },
  {
    "id": "cat_fashion",
    "name": "Fashion",
    "parentId": null,
    "children": [
      { "id": "cat_mens_shirts", "name": "Men's Shirts", "parentId": "cat_fashion", "children": [] },
      { "id": "cat_womens_dresses", "name": "Women's Dresses", "parentId": "cat_fashion", "children": [] }
    ]
  }
]
```

### 2.2 Direct children of a category

`GET /api/v1/storefront/categories/{categoryId}/children`

Response `200`:

```json
{
  "parentId": "cat_electronics",
  "children": [
    { "id": "cat_mobile_phones", "name": "Mobile Phones", "parentId": "cat_electronics", "active": true, "listingAllowed": true, "c2cAllowed": false },
    { "id": "cat_laptops",       "name": "Laptops",       "parentId": "cat_electronics", "active": true, "listingAllowed": true, "c2cAllowed": false }
  ]
}
```

---

# 3. Products — Browse & Search **[new, built on existing shapes]**

Storefront product cards = catalog `ProductSearchResponse` enriched with first image, price range and stock flag.
Search request mirrors `ProductSearchRequest` (`query`, `categoryId`, statuses fixed server-side to sellable only).

### 3.1 List / search products

`POST /api/v1/storefront/products/search?page=0&size=20&sort=name,asc`

Request body (all optional):

```json
{
  "query": "wireless earbuds",
  "categoryId": "cat_electronics",
  "condition": "NEW",
  "minPrice": 10000,
  "maxPrice": 500000,
  "featured": false
}
```

Response `200` → `PagedModel<StorefrontProductCard>`:

```json
{
  "content": [
    {
      "productId": "prd_01J9F5A2B4C6D8E0F2G4H6J8K0",
      "productName": "SoundPeats Wireless Earbuds Air3 Lite",
      "slug": "soundpeats-wireless-earbuds-air3-lite",
      "categoryName": "Mobile Phones",
      "categoryId": "cat_mobile_phones",
      "condition": "NEW",
      "featured": true,
      "thumbnailUrl": "https://cdn.example.com/media/prd_01J9F5A2/air3-lite-front.webp",
      "priceRange": {
        "minAmount": 45900,
        "maxAmount": 45900,
        "currencyCode": "mmk"
      },
      "inStock": true,
      "sellerId": "mrc_01H8X2Y4Z6A8B0C2D4E6F8G0H2"
    },
    {
      "productId": "prd_01J9F5C4D6E8F0A2B4C6D8E0F2",
      "productName": "Anker Soundcore Life P2 Mini",
      "slug": "anker-soundcore-life-p2-mini",
      "categoryName": "Mobile Phones",
      "categoryId": "cat_mobile_phones",
      "condition": "NEW",
      "featured": false,
      "thumbnailUrl": "https://cdn.example.com/media/prd_01J9F5C4/life-p2-mini.webp",
      "priceRange": {
        "minAmount": 62900,
        "maxAmount": 75900,
        "currencyCode": "mmk"
      },
      "inStock": false,
      "sellerId": "mrc_01H8X2Y4Z6A8B0C2D4E6F8G0H2"
    }
  ],
  "_links": {
    "self": { "href": "/api/v1/storefront/products/search?page=0&size=20" },
    "next": { "href": "/api/v1/storefront/products/search?page=1&size=20" }
  },
  "page": { "size": 20, "number": 0, "totalElements": 34, "totalPages": 2 }
}
```

Field notes:

| Field | Type | Notes |
|---|---|---|
| `productId` | string | catalog product id |
| `productName`, `slug`, `categoryName`, `categoryId` | string | from `ProductSearchResponse` |
| `condition` | string | `NEW \| USED \| REFURBISHED` (`ListingCondition`) |
| `featured` | boolean | catalog flag |
| `thumbnailUrl` | string | first product media (`Media.path`) resolved to CDN URL |
| `priceRange.minAmount/maxAmount` | number | min/max sellable variant price |
| `priceRange.currencyCode` | string | lowercase ISO code |
| `inStock` | boolean | at least one variant with `availableQuantity > 0` |

### 3.2 Featured / home sections

Same response shape as 3.1. Convenience endpoints:

- `GET /api/v1/storefront/products/featured?page=0&size=10`
- `GET /api/v1/storefront/products/new-arrivals?page=0&size=10`

### 3.3 Product detail

`GET /api/v1/storefront/products/slug/{slug}`

Built on `GetProductBySlugResponse`, each variant enriched with live price + stock (pricing/inventory read models).

Response `200`:

```json
{
  "id": "prd_01J9F5A2B4C6D8E0F2G4H6J8K0",
  "name": "SoundPeats Wireless Earbuds Air3 Lite",
  "categoryId": "cat_smartphones",
  "sellerId": "mrc_01H8X2Y4Z6A8B0C2D4E6F8G0H2",
  "sellerType": "FIRST_PARTY",
  "condition": "NEW",
  "offerEligible": true,
  "status": "ACTIVE",
  "slug": "soundpeats-wireless-earbuds-air3-lite",
  "featured": true,
  "descriptions": [
    {
      "id": "desc_01A1",
      "name": "overview",
      "title": "Overview",
      "description": "Bluetooth 5.2 earbuds with 25h total playtime and ENC calls."
    },
    {
      "id": "desc_01A2",
      "name": "specifications",
      "title": "Specifications",
      "description": "Driver: 12mm\nBattery: 40mAh buds / 450mAh case\nCharging: USB-C"
    }
  ],
  "medias": [
    { "id": "med_01M1", "type": "IMAGE", "path": "https://cdn.example.com/media/prd_01J9F5A2/air3-lite-front.webp" },
    { "id": "med_01M2", "type": "IMAGE", "path": "https://cdn.example.com/media/prd_01J9F5A2/air3-lite-case.webp" },
    { "id": "med_01M3", "type": "VIDEO", "path": "https://cdn.example.com/media/prd_01J9F5A2/air3-lite-demo.mp4" }
  ],
  "variantTypes": [
    {
      "typeId": "vt_color",
      "typeName": "Color",
      "options": [
        { "optionId": "vo_black", "optionName": "Black" },
        { "optionId": "vo_white", "optionName": "White" }
      ]
    }
  ],
  "variants": [
    {
      "id": "var_01J9F5A2B4C6D8E0F2G4H6J8K0_BK",
      "sku": "SPE-AIR3-BLK",
      "status": "ACTIVE",
      "variations": [
        { "typeId": "vt_color", "typeName": "Color", "optionId": "vo_black", "optionName": "Black" }
      ],
      "price": {
        "amount": 45900,
        "originalAmount": null,
        "currencyCode": "mmk"
      },
      "stock": {
        "availableQuantity": 42,
        "inStock": true
      }
    },
    {
      "id": "var_01J9F5A2B4C6D8E0F2G4H6J8K0_WH",
      "sku": "SPE-AIR3-WHT",
      "status": "ACTIVE",
      "variations": [
        { "typeId": "vt_color", "typeName": "Color", "optionId": "vo_white", "optionName": "White" }
      ],
      "price": {
        "amount": 47900,
        "originalAmount": 55900,
        "currencyCode": "mmk"
      },
      "stock": {
        "availableQuantity": 0,
        "inStock": false
      }
    }
  ],
  "_links": {
    "self": { "href": "/api/v1/storefront/products/slug/soundpeats-wireless-earbuds-air3-lite" }
  }
}
```

Notes:

- `variantTypes[].options` drives the RN option selector; pick one option per `typeId`, then match a `variant` whose `variations` contains all selected options.
- `variant.status`: `ACTIVE | DELETED | ARCHIVED` — app should display only `ACTIVE`.
- Variant `price.originalAmount` is non-null when discounted (strikethrough UI); mirrors `CalculatedPriceSetResponse.originalAmount`.
- `stock.availableQuantity` comes from inventory `AllocationAvailabilityResponse`; when `<= 5` show low-stock badge.

---

# 4. Cart **[new]**

Cart is server-side per customer (identified by access token). Quantities reference variant `sku`.

### 4.1 Get cart

`GET /api/v1/storefront/cart`

Response `200`:

```json
{
  "cartId": "crt_01JAA0B2C4D6E8F0A2B4C6D8E0",
  "customerId": "01J9F3K2M8N4P6Q8R0T2V4X6Z8",
  "items": [
    {
      "itemId": "cit_01JAA1D4F6H8J0L2N4P6R8T0V2",
      "productId": "prd_01J9F5A2B4C6D8E0F2G4H6J8K0",
      "productName": "SoundPeats Wireless Earbuds Air3 Lite",
      "slug": "soundpeats-wireless-earbuds-air3-lite",
      "variantId": "var_01J9F5A2B4C6D8E0F2G4H6J8K0_BK",
      "sku": "SPE-AIR3-BLK",
      "options": [
        { "typeId": "vt_color", "typeName": "Color", "optionId": "vo_black", "optionName": "Black" }
      ],
      "unitPrice": { "amount": 45900, "originalAmount": null, "currencyCode": "mmk" },
      "quantity": 2,
      "availableQuantity": 42,
      "inStock": true,
      "lineTotal": { "amount": 91800, "currencyCode": "mmk" },
      "thumbnailUrl": "https://cdn.example.com/media/prd_01J9F5A2/air3-lite-front.webp"
    }
  ],
  "totals": {
    "itemCount": 2,
    "subtotal": { "amount": 91800, "currencyCode": "mmk" },
    "discountTotal": { "amount": 0, "currencyCode": "mmk" },
    "shippingFee": { "amount": 3000, "currencyCode": "mmk" },
    "taxTotal": { "amount": 0, "currencyCode": "mmk" },
    "grandTotal": { "amount": 94800, "currencyCode": "mmk" }
  },
  "_links": {
    "self": { "href": "/api/v1/storefront/cart" }
  }
}
```

Empty cart returns `"items": []` with zeroed totals.

### 4.2 Add item

`POST /api/v1/storefront/cart/items`

```json
{
  "sku": "SPE-AIR3-BLK",
  "quantity": 2
}
```

Response `201` → full cart (same shape as 4.1). Adding an existing `sku` merges quantity.

Errors:

- `409` when `quantity > availableQuantity` → `ProblemDetail.detail: "Insufficient stock"`.

### 4.3 Update item quantity

`PATCH /api/v1/storefront/cart/items/{itemId}`

```json
{ "quantity": 3 }
```

Response `200` → full cart. `quantity: 0` removes the item.

### 4.4 Remove item

`DELETE /api/v1/storefront/cart/items/{itemId}`

Response `204`. Response body empty; client re-fetches cart or uses previous state minus item.

---

# 5. Checkout **[new]**

### 5.1 Preview checkout (totals before placing order)

`GET /api/v1/storefront/checkout/preview`

Response `200`:

```json
{
  "cartId": "crt_01JAA0B2C4D6E8F0A2B4C6D8E0",
  "itemsCount": 2,
  "totals": {
    "subtotal":     { "amount": 91800, "currencyCode": "mmk" },
    "discountTotal":{ "amount": 5000,  "currencyCode": "mmk" },
    "shippingFee":  { "amount": 3000,  "currencyCode": "mmk" },
    "taxTotal":     { "amount": 0,     "currencyCode": "mmk" },
    "grandTotal":   { "amount": 89800, "currencyCode": "mmk" }
  },
  "paymentMethods": [
    { "code": "COD",  "name": "Cash on Delivery", "enabled": true },
    { "code": "KBZ_PAY", "name": "KBZPay", "enabled": true },
    { "code": "WAVE_PAY", "name": "WavePay", "enabled": false }
  ],
  "shippingMethods": [
    { "code": "STANDARD", "name": "Standard Delivery", "fee": { "amount": 3000, "currencyCode": "mmk" }, "etaDays": "3-5" },
    { "code": "EXPRESS",  "name": "Express Delivery",  "fee": { "amount": 6500, "currencyCode": "mmk" }, "etaDays": "1-2" }
  ],
  "issues": []
}
```

`issues` lists blocking problems, e.g.:

```json
{
  "issues": [
    { "code": "OUT_OF_STOCK", "sku": "SPE-AIR3-WHT", "message": "White variant is out of stock. Remove it to continue." }
  ]
}
```

### 5.2 Place order

`POST /api/v1/storefront/orders`

```json
{
  "cartId": "crt_01JAA0B2C4D6E8F0A2B4C6D8E0",
  "shippingAddress": {
    "fullName": "Aung Aung",
    "phone": "+959123456789",
    "city": "Yangon",
    "township": "Kamayut",
    "addressLine1": "No. 123, Pyay Road",
    "addressLine2": "Apartment 4B",
    "postalCode": "11041",
    "notes": "Call before delivery"
  },
  "shippingMethod": "STANDARD",
  "paymentMethod": "COD"
}
```

Field constraints:

| Field | Required | Notes |
|---|---|---|
| `cartId` | yes | must belong to caller |
| `shippingAddress.fullName`, `phone`, `city`, `addressLine1` | yes | |
| `shippingMethod` | yes | one of preview `shippingMethods[].code` |
| `paymentMethod` | yes | one of enabled `paymentMethods[].code` |

Success `201` → Order confirmation (same shape as order detail, §6.2):

```json
{
  "orderId": "ord_01JAB2F6J0K4O8S2W6A0E4I8M2",
  "orderNumber": "SO-20260825-000123",
  "status": "PENDING",
  "paymentMethod": "COD",
  "shippingMethod": "STANDARD",
  "placedAt": "2026-08-25T11:02:14Z",
  "customer": {
    "customerId": "01J9F3K2M8N4P6Q8R0T2V4X6Z8",
    "email": "aung.aung@example.com"
  },
  "shippingAddress": {
    "fullName": "Aung Aung",
    "phone": "+959123456789",
    "city": "Yangon",
    "township": "Kamayut",
    "addressLine1": "No. 123, Pyay Road",
    "addressLine2": "Apartment 4B",
    "postalCode": "11041",
    "notes": "Call before delivery"
  },
  "items": [
    {
      "itemId": "oit_01JAB2F6J0K4O8S2W6A0E4I8N4",
      "productId": "prd_01J9F5A2B4C6D8E0F2G4H6J8K0",
      "productName": "SoundPeats Wireless Earbuds Air3 Lite",
      "slug": "soundpeats-wireless-earbuds-air3-lite",
      "sku": "SPE-AIR3-BLK",
      "options": [
        { "typeId": "vt_color", "typeName": "Color", "optionId": "vo_black", "optionName": "Black" }
      ],
      "unitPrice": { "amount": 45900, "currencyCode": "mmk" },
      "quantity": 2,
      "lineTotal": { "amount": 91800, "currencyCode": "mmk" },
      "thumbnailUrl": "https://cdn.example.com/media/prd_01J9F5A2/air3-lite-front.webp"
    }
  ],
  "totals": {
    "subtotal":      { "amount": 91800, "currencyCode": "mmk" },
    "discountTotal": { "amount": 5000,  "currencyCode": "mmk" },
    "shippingFee":   { "amount": 3000,  "currencyCode": "mmk" },
    "taxTotal":      { "amount": 0,     "currencyCode": "mmk" },
    "grandTotal":    { "amount": 89800, "currencyCode": "mmk" }
  },
  "_links": {
    "self": { "href": "/api/v1/storefront/orders/ord_01JAB2F6J0K4O8S2W6A0E4I8M2" }
  }
}
```

On success the referenced cart is cleared. Failure responses are `ProblemDetail` (§ Conventions):

| Status | When |
|---|---|
| `400` | validation failed |
| `409` | item went out of stock / price changed since preview (`detail` explains) |
| `404` | cart not found |

---

# 6. Orders **[new]**

### 6.1 My orders

`GET /api/v1/storefront/orders?page=0&size=20&sort=placedAt,desc`

Response `200` → `PagedModel<OrderSummary>`:

```json
{
  "content": [
    {
      "orderId": "ord_01JAB2F6J0K4O8S2W6A0E4I8M2",
      "orderNumber": "SO-20260825-000123",
      "status": "CONFIRMED",
      "placedAt": "2026-08-25T11:02:14Z",
      "itemCount": 2,
      "grandTotal": { "amount": 89800, "currencyCode": "mmk" },
      "thumbnailUrls": [
        "https://cdn.example.com/media/prd_01J9F5A2/air3-lite-front.webp"
      ]
    }
  ],
  "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
}
```

### 6.2 Order detail

`GET /api/v1/storefront/orders/{orderId}` → full order shape identical to §5.2 response.

Order `status` state machine (display mapping suggestion):

| status | meaning | RN badge |
|---|---|---|
| `PENDING` | placed, awaiting confirmation | amber |
| `CONFIRMED` | confirmed, preparing | blue |
| `SHIPPED` | handed to courier | indigo |
| `DELIVERED` | received | green |
| `CANCELLED` | cancelled | red |

### 6.3 Cancel order

`POST /api/v1/storefront/orders/{orderId}/cancel`

```json
{ "reason": "Changed my mind" }
```

Response `200` → updated order detail with `"status": "CANCELLED"`. `409` if already shipped/delivered.

---

# 7. Shared value objects (reference for mocks/types)

### Money

```json
{ "amount": 45900, "currencyCode": "mmk" }
```

With discount context (product/variant):

```json
{ "amount": 47900, "originalAmount": 55900, "currencyCode": "mmk" }
```

### Selected variation options

```json
{ "typeId": "vt_color", "typeName": "Color", "optionId": "vo_black", "optionName": "Black" }
```

### Enums (serialized values used across payloads)

| Enum | Values | Used in |
|---|---|---|
| `ProductStatus` | `DRAFT, ACTIVE, ARCHIVED, SUSPENDED` | storefront always returns `ACTIVE` |
| `ListingCondition` | `NEW, USED, REFURBISHED` | product card/detail filter |
| `ProductVariantStatus` | `ACTIVE, DELETED, ARCHIVED` | variant visibility |
| Order status **[new]** | `PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED` | orders |
| Media type **[new]** | `IMAGE, VIDEO` | product medias |

---

# 8. Mock dataset notes (for RN development)

Consistent entities reused across all mock payloads above:

| Entity | Sample ids |
|---|---|
| Customer | `01J9F3K2M8N4P6Q8R0T2V4X6Z8` / `aung.aung@example.com` |
| Seller | `mrc_01H8X2Y4Z6A8B0C2D4E6F8G0H2` (FIRST_PARTY) |
| Products | `prd_01J9F5A2B4C6D8E0F2G4H6J8K0` (earbuds, 2 color variants), `prd_01J9F5C4D6E8F0A2B4C6D8E0F2` (out of stock earbuds) |
| SKUs | `SPE-AIR3-BLK` (in stock), `SPE-AIR3-WHT` (out of stock) |
| Categories | `cat_electronics > cat_mobile_phones > cat_smartphones`, `cat_fashion > ...` |
| Cart | `crt_01JAA0B2C4D6E8F0A2B4C6D8E0` |
| Order | `ord_01JAB2F6J0K4O8S2W6A0E4I8M2` / `SO-20260825-000123` |
| Currency | `mmk`, amounts as plain JSON numbers (BigDecimal over the wire) |

Suggested mock flow for the app:

1. Login (mock returns `AuthResponse`) → store `accessToken`
2. Home: `categories/tree` + `products/featured` + `products/new-arrivals`
3. Category tab → `products/search { categoryId }` (paginated, infinite scroll via `page.number+1`)
4. Search bar → `products/search { query }`
5. Detail → `products/slug/{slug}`, select options → matched variant → Add to cart
6. Cart screen → `GET cart`, qty steppers via `PATCH items/{itemId}`
7. Checkout → `checkout/preview` → fill address form → `POST orders`
8. Orders tab → `orders` list → detail → cancel
