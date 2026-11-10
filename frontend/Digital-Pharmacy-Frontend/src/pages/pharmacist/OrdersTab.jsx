import { useState } from 'react';
import { money } from '../../data/products';
import PageTitle from './PageTitle';

export default function OrdersTab({
  orders,
  ordersLoading,
  ordersError,
  loadOrders,
  packOrder,
  collectOrder,
  busyOrder,
  badge
}) {
  const [selectedOrder, setSelectedOrder] = useState(null);

  return (
    <>
      <section>
        <PageTitle
          title="Prepare orders"
          subtitle="Load real orders, check payment and update fulfilment status."
        />

        <div className="ph-info-banner">
          <span>▣</span>
          <div>
            <strong>Dispensing checklist</strong>
            <p>
              Confirm payment, medicine, strength, quantity and prescription
              approval before packing an order.
            </p>
          </div>
        </div>

        {ordersError && (
          <div className="ph-info-banner" role="alert">
            <span>!</span>
            <div>
              <strong>Orders could not be fully loaded or updated</strong>
              <p>{ordersError}</p>
            </div>
          </div>
        )}

        <div className="ph-panel">
          <div className="ph-panel-heading">
            <div>
              <h2>Orders in pharmacy</h2>
              <p>
                {orders.length} backend order
                {orders.length === 1 ? '' : 's'}
              </p>
            </div>

            <button
              className="ph-btn ph-btn-outline"
              onClick={loadOrders}
              disabled={ordersLoading}
            >
              {ordersLoading ? 'Refreshing…' : 'Refresh orders'}
            </button>
          </div>

          {ordersLoading && orders.length === 0 ? (
            <div className="ph-empty">Loading orders from the backend…</div>
          ) : (
            <div className="ph-table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Order</th>
                    <th>Customer</th>
                    <th>Items</th>
                    <th>Placed</th>
                    <th>Order status</th>
                    <th>Payment</th>
                    <th>Action</th>
                  </tr>
                </thead>

                <tbody>
                  {orders.map(order => {
                    const id = order.orderNumber ?? order.id;
                    const status =
                      order.orderStatus || order.status || 'UNKNOWN';
                    const paid = order.paymentStatus === 'PAID';
                    const canPack = paid && status === 'PROCESSING';
                    const canCollect =
                      String(status).toUpperCase() ===
                        'READY_FOR_COLLECTION' &&
                      String(order.orderType || order.type).toUpperCase() ===
                        'CLICK_AND_COLLECT';
                    const isOrderBusy = busyOrder === id;

                    return (
                      <tr key={id}>
                        <td>
                          <strong>{id}</strong>
                        </td>

                        <td>
                          {order.customer}
                          {order.customerEmail && (
                            <span
                              className="ph-muted"
                              style={{ display: 'block' }}
                            >
                              {order.customerEmail}
                            </span>
                          )}
                        </td>

                        <td>{order.items}</td>
                        <td>{order.placed}</td>
                        <td>{badge(status.replaceAll('_', ' '))}</td>
                        <td>{badge(order.paymentStatus || 'UNKNOWN')}</td>

                        <td>
                          <button
                            className="ph-btn ph-btn-small"
                            onClick={() => setSelectedOrder(order)}
                            disabled={isOrderBusy}
                          >
                            View items
                          </button>
                          {canCollect && (
                            <button
                              className="ph-btn ph-btn-small"
                              onClick={() => collectOrder(order)}
                              disabled={isOrderBusy}
                              style={{ display: 'block', marginTop: 5 }}
                            >
                              {isOrderBusy ? 'Saving…' : 'Mark collected'}
                            </button>
                          )}
                          {canPack ? (
                            <span
                              className="ph-muted"
                              style={{ display: 'block', marginTop: 5 }}
                            >
                              Review before packing
                            </span>
                          ) : canCollect ? null : (
                            <span
                              className="ph-muted"
                              style={{ display: 'block', marginTop: 5 }}
                            >
                              {status === 'CANCELLED'
                                ? 'Cancelled'
                                : [
                                    'PACKED',
                                    'READY_FOR_COLLECTION',
                                    'SHIPPED',
                                    'DELIVERED',
                                    'COLLECTED'
                                  ].includes(status)
                                  ? 'No action needed'
                                  : !paid
                                    ? 'Awaiting payment'
                                    : 'Review status'}
                            </span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>

              {!ordersLoading && orders.length === 0 && (
                <div className="ph-empty">
                  No orders were returned by the backend.
                </div>
              )}
            </div>
          )}
        </div>

        <p className="ph-disclaimer">
          Order data and packing actions use the backend API. Always confirm
          prescription approval before dispensing.
        </p>
      </section>

      {selectedOrder && (
        <div
          className="ph-modal-backdrop"
          role="presentation"
          onMouseDown={event => {
            if (event.target === event.currentTarget) {
              setSelectedOrder(null);
            }
          }}
        >
          <section
            className="ph-review-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="ph-order-items-title"
          >
            <button
              className="ph-modal-close"
              onClick={() => setSelectedOrder(null)}
              aria-label="Close order details"
            >
              ×
            </button>
            <span className="ph-badge info">Order details</span>
            <h2 id="ph-order-items-title">
              Order {selectedOrder.orderNumber ?? selectedOrder.id}
            </h2>
            <p className="ph-muted">
              Review each item and quantity before packing this order.
            </p>
            <div className="ph-table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Item</th>
                    <th>Quantity</th>
                    <th>Unit price</th>
                  </tr>
                </thead>
                <tbody>
                  {(Array.isArray(selectedOrder.orderItems)
                    ? selectedOrder.orderItems
                    : []
                  ).map((line, index) => (
                    <tr key={line.id ?? line.orderItemId ?? index}>
                      <td>
                        <strong>
                          {line.product?.productName ||
                            line.product?.name ||
                            line.productName ||
                            line.name ||
                            `Product ${line.productId ?? ''}`}
                        </strong>
                      </td>
                      <td>{line.orderQty ?? line.quantity ?? 1}</td>
                      <td>
                        {line.unitPrice != null
                          ? money(line.unitPrice)
                          : line.price != null
                            ? money(line.price)
                            : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {(!Array.isArray(selectedOrder.orderItems) ||
                selectedOrder.orderItems.length === 0) && (
                <div className="ph-empty">
                  No item lines were returned for this order. Check the order
                  API response before packing.
                </div>
              )}
            </div>
            <div className="ph-modal-actions">
              <button
                className="ph-btn"
                onClick={() => setSelectedOrder(null)}
              >
                Close
              </button>
              {String(selectedOrder.paymentStatus || '').toUpperCase() ===
                'PAID' &&
                ['PROCESSING', 'PENDING'].includes(
                  String(
                    selectedOrder.orderStatus || selectedOrder.status || ''
                  ).toUpperCase()
                ) && (
                  <button
                    className="ph-btn ph-btn-primary"
                    disabled={
                      busyOrder ===
                      (selectedOrder.orderNumber ?? selectedOrder.id)
                    }
                    onClick={async () => {
                      await packOrder(selectedOrder);
                      setSelectedOrder(null);
                    }}
                  >
                    {busyOrder ===
                    (selectedOrder.orderNumber ?? selectedOrder.id)
                      ? 'Packing…'
                      : 'Mark as Packed'}
                  </button>
                )}
            </div>
          </section>
        </div>
      )}
    </>
  );
}
