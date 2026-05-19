package com.elshimma.erp.inventory.entity;

/**
 * Types of inventory movements.
 *
 * IN         — stock received (purchase, production output)
 * OUT        — stock consumed or shipped (order fulfillment, production input)
 * ADJUSTMENT — manual correction (stocktake fix, damage write-off)
 * TRANSFER   — moved between warehouses
 * RESERVED   — stock reserved for a pending order
 * RELEASED   — previously reserved stock released back to available
 */
public enum MovementType {
    IN,
    OUT,
    ADJUSTMENT,
    TRANSFER,
    RESERVED,
    RELEASED
}
