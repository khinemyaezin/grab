# Feature: Product dashboard

## 1. Problem Statement
The system needs to support product list to display products for admin.

## 2. Business Scope

### In scope
- display product list of seller.

### Out of scope 
- doesnt include pricing related values.

### Input Schema / API Request
```json
{
  "productName": "string | undefined",
  "sku": "string | undefined",
  "variantStatus": "string | undefined",
  "categoryId": "string | undefined",
  "productStatus": "string | undefined",
  "page": "number",
  "size": "number"
}
```
### Output Schema / API Response
```json
{
  "products": [
    {
      "id": "string",
      "name": "string",
      "status": "string",
      "slug": "string",
      "categoryName": "string",
      "variant": {
        "available": "boolean",
        "types": [
          {
            "typeId": "string",
            "typeName": "string",
            "options": [
              {
                "optionId": "string",
                "optionName": "string"
              }
            ]
          }
        ]
      }
    }
  ],
  "pageInfo": {
    "page": "number",
    "size": "number",
    "totalElements": "number",
    "totalPages": "number"
  }
}
```