package za.ac.cput.digitalpharmacysystem.service;
/*
 * Sibulele Pama
 * 221447498
 */

import za.ac.cput.digitalpharmacysystem.domain.Order;
import za.ac.cput.digitalpharmacysystem.dto.PlaceOrderRequest;

import java.util.List;

/**
 * Orders are not created or edited as raw entities (that would bypass stock, prescriptions and
 * deliveries), so this does not extend IService.
 *
 * Life of an order:
 *   placeOrder -> PENDING / payment PENDING
 *   pay        -> PROCESSING / PAID
 *   pack       -> DELIVERY: PACKED (then the delivery moves it to SHIPPED, DELIVERED)
 *                 CLICK_AND_COLLECT: READY_FOR_COLLECTION
 *   collect    -> COLLECTED
 *   cancelOrder is possible until the order has been shipped or collected.
 */
public interface IOrderService {

    /**
     * Validates the items, checks prescriptions and refills for prescription-only products,
     * deducts stock, creates the delivery (for DELIVERY orders) and saves the order,
     * all in one transaction.
     */
    Order placeOrder(PlaceOrderRequest request);

    Order read(String orderNumber);

    List<Order> getAll();

    List<Order> getOrdersForCurrentUser();

    /** Order history for one customer, newest first. */
    List<Order> getByCustomer(String customerId);

    /** Records payment (simulated, there is no payment gateway) and starts processing. */
    Order pay(String orderNumber);

    /** Pharmacist has packed the order. */
    Order pack(String orderNumber);

    /** Customer has collected a Click and Collect order. */
    Order collect(String orderNumber);

    /** Puts stock, refills and the delivery back; possible until shipped or collected. */
    Order cancelOrder(String orderNumber);

    /** Deletes an order; if it still holds stock or refills they are given back first. */
    boolean delete(String orderNumber);
}
