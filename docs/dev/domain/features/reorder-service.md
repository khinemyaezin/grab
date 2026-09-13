# Reorder Service Feature

## Overview

The Reorder Service analyses current inventory levels against configured thresholds and generates prioritised reorder suggestions. It is a **read-only** domain service — it never modifies inventory state.

---

## Business Rules

### Calculate Priority

Determines urgency for a single inventory item.

**Inputs from the item**:
- `available` = `item.getAvailableQuantity()` — same formula as [Inventory Allocation](inventory-allocation.md)
- `safetyStock` = `item.getReorderConfig().safetyStock()`
- `reorderPoint` = `item.getReorderConfig().reorderPoint()`

**Decision chain**

| Condition | Result |
|-----------|--------|
| `available <= 0` | `CRITICAL` |
| `available <= safetyStock` | `CRITICAL` |
| `available <= reorderPoint` | `HIGH` |
| `available <= reorderPoint * 1.2` | `MEDIUM` |
| none of the above | `LOW` |

**Example** (safetyStock = 10, reorderPoint = 50):

| Available | Priority | Reason |
|-----------|----------|--------|
| 0 | CRITICAL | Out of stock |
| 8 | CRITICAL | Below safetyStock (10) |
| 10 | CRITICAL | Equal to safetyStock |
| 11 | HIGH | Above safetyStock, at or below reorderPoint |
| 50 | HIGH | Equal to reorderPoint |
| 55 | MEDIUM | Within 20% above reorderPoint (≤ 60) |
| 60 | MEDIUM | Equal to reorderPoint × 1.2 |
| 61 | LOW | Above all thresholds |

---

### Calculate Reorder Suggestions

**Business logic**:

Should apply to all searching methods by sku or location.
```
1. Calculate on all inventory items
2. Filter item is active - keeps only status == ACTIVE
3. Excludes LOW priority
4. Sort by priority (CRITICAL → HIGH → MEDIUM)
```

---

### Calculate Reorder Suggestions For Location

**Business logic**:
```
1. Query by location
2. Filter item is active - keeps only status == ACTIVE
3. Excludes LOW priority
4. Sort by priority (CRITICAL → HIGH → MEDIUM)
```

---

### Calculate Reorder Suggestions For Sku

**Business logic**:
```
1. Query by sku
2. Filter item is active - keeps only status == ACTIVE
3. Excludes LOW priority
4. Sort by priority (CRITICAL → HIGH → MEDIUM)
```

---

### Get Critical Reorder Items

**Business logic**:
```
1. Query out of stock items & low stock items
2. Filter only active items - keeps only status == ACTIVE
5. Must be unique items (an item can be both out of stock and low stock, but should only appear once)
```

---

## Summary of What Is and Is NOT Enforced

### Enforced ✅
- Only ACTIVE items appear in suggestions
- LOW priority items excluded from suggestions
- Suggestions sorted by priority (CRITICAL first)
- Duplicates removed in `getCriticalReorderItems`
- Priority is deterministic based on item state
- `suggestedQuantity` respects `maxStock` cap
- `reorderPoint >= safetyStock` enforced by `ReorderConfig` constructor

### Not Enforced ❌
- No input validation on `locationId` or `sku` parameters
- No pagination on results
- No caching — every call queries the repository
- No secondary sort within the same priority level
- No sorting in `getCriticalReorderItems`
- MEDIUM threshold multiplier (1.2×) is hardcoded
- `inTransit` stock not considered in priority calculation
- `calculatePriority` does not check item status (caller must filter)
- `calculatePriority` is called twice per item in suggestion pipeline (once in filter, once in map)
- No events published (entirely read-only service)
- No distinction between "needs reorder" and "reorder already placed"
