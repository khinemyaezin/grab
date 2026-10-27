package com.grab.app.platform.seller.port.rest;

/**
 * Marker contract for REST endpoints owned by the Seller BFF.
 *
 * Endpoint signatures stay on the concrete public contracts as they are split out
 * incrementally; the marker keeps every controller inside the BFF boundary.
 */
public interface SellerRestApi {
}
