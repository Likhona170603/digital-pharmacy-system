import React, { useCallback, useEffect, useState } from 'react';
import api from '../services/api';
import { money } from '../data/products';
import '../styles/orders.css';

const ORDER_STATUS = {
  PENDING: { label: 'Awaiting payment', tone: 'warn' },
  PROCESSING: { label: 'Being prepared', tone: 'warn' },
  PACKED: { label: 'Packed, awaiting courier', tone: 'warn' },
  SHIPPED: { label: 'Shipped', tone: 'warn' },
  DELIVERED: { label: 'Delivered', tone: 'good' },
  READY_FOR_COLLECTION: { label: 'Ready for collection', tone: 'good' },
  COLLECTED: { label: 'Collected', tone: 'good' },
  CANCELLED: { label: 'Cancelled', tone: 'bad' }
};

const PAYMENT_STATUS = {
  PENDING: { label: 'Unpaid', tone: 'warn' },
  PAID: { label: 'Paid', tone: 'good' },
  DECLINED: { label: 'Declined', tone: 'bad' },
  REFUNDED: { label: 'Refunded', tone: 'neutral' }
};

const CANCELLABLE = ['PENDING', 'PROCESSING', 'PACKED', 'READY_FOR_COLLECTION'];

function formatDate(value) {
  if (!value) return '';

  // Spring can send a date-time as an ISO string or as [y, m, d, h, min, s].
  const date = Array.isArray(value)
    ? new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0)
    : new Date(value);

  if (Number.isNaN(date.getTime())) return '';

  return date.toLocaleString([], {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function describeError(err, fallback) {
  if (err instanceof TypeError) {
    return 'Cannot connect to the server. Make sure the backend is running.';
  }
  if (err.status === 401 || err.status === 403) {
    return 'Your session has expired. Please log in again.';
  }
  return err.message || fallback;
}

export default function Orders({ user, onBrowse, onNotify }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busyId, setBusyId] = useState('');

  const userId = user?.id;

  const load = useCallback(async () => {
    if (userId === undefined || userId === null) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = await api.get(`/order/customer/${encodeURIComponent(String(userId))}`);
      setOrders(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(describeError(err, 'Could not load your orders.'));
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    load();
  }, [load]);

  async function runAction(order, action, successMessage) {
    setBusyId(order.orderNumber);
    setError('');

    try {
      await api.post(`/order/${encodeURIComponent(order.orderNumber)}/${action}`);
      onNotify?.(successMessage);
      await load();
    } catch (err) {
      setError(describeError(err, 'That action could not be completed.'));
    } finally {
      setBusyId('');
    }
  }

  function payOrder(order) {
    runAction(order, 'pay', 'Payment received. Your order is being prepared.');
  }

  function cancelOrder(order) {
    const ok = window.confirm(
      'Cancel this order? The items are released back to stock' +
        (order.paymentStatus === 'PAID' ? ' and your payment is refunded.' : '.')
    );

    if (ok) runAction(order, 'cancel', 'Your order was cancelled.');
  }

  if (userId === undefined || userId === null) {
    return (
      <main className="orders-page">
        <h1>My Orders</h1>
        <p>Please log in to see your orders.</p>
      </main>
    );
  }

  return (
    <main className="orders-page">
      <div className="orders-head">
        <h1>My Orders</h1>
        <button className="hp-btn hp-btn-ghost" onClick={load} disabled={loading}>
          {loading ? 'Loading…' : 'Refresh'}
        </button>
      </div>

      {error && (
        <div role="alert" className="orders-message orders-message-error">
          {error}
        </div>
      )}

      {!loading && !error && orders.length === 0 && (
        <section className="orders-empty">
          <p>You have not placed any orders yet.</p>
          <button className="hp-btn hp-btn-primary" onClick={onBrowse}>
            Browse Products
          </button>
        </section>
      )}

      <div className="orders-list">
        {orders.map(order => {
          const status = ORDER_STATUS[order.orderStatus] || {
            label: order.orderStatus,
            tone: 'neutral'
          };
          const payment = PAYMENT_STATUS[order.paymentStatus] || {
            label: order.paymentStatus,
            tone: 'neutral'
          };
          const canPay = order.orderStatus === 'PENDING' && order.paymentStatus === 'PENDING';
          const canCancel = CANCELLABLE.includes(order.orderStatus);
          const busy = busyId === order.orderNumber;
          const shortNumber = String(order.orderNumber || '')
            .replace(/^ORD-/, '')
            .slice(0, 8)
            .toUpperCase();

          return (
            <article className="orders-card" key={order.orderNumber}>
              <header className="orders-card-head">
                <div>
                  <h2>Order {shortNumber}</h2>
                  <p>
                    {formatDate(order.createdAt)}
                    {' · '}
                    {order.orderType === 'DELIVERY' ? 'Delivery' : 'Collect from pharmacy'}
                  </p>
                </div>
                <div className="orders-badges">
                  <span className={`orders-badge orders-${status.tone}`}>{status.label}</span>
                  <span className={`orders-badge orders-${payment.tone}`}>{payment.label}</span>
                </div>
              </header>

              <ul className="orders-items">
                {(order.items || []).map(item => (
                  <li key={item.orderItemId}>
                    <span>
                      {item.product?.productName || 'Product'} × {item.orderQty}
                      {(item.prescriptionId || item.product?.rxRequired) && (
                        <em className="orders-rx">Rx</em>
                      )}
                    </span>
                    <span>{money(Number(item.priceAtPurchase || 0) * item.orderQty)}</span>
                  </li>
                ))}
              </ul>

              <footer className="orders-card-foot">
                <strong>Total {money(order.totalAmt)}</strong>
                <div className="orders-actions">
                  {canPay && (
                    <button
                      className="hp-btn hp-btn-primary"
                      onClick={() => payOrder(order)}
                      disabled={busy}
                    >
                      {busy ? 'Working…' : 'Pay now'}
                    </button>
                  )}
                  {canCancel && (
                    <button
                      className="hp-btn hp-btn-ghost"
                      onClick={() => cancelOrder(order)}
                      disabled={busy}
                    >
                      Cancel order
                    </button>
                  )}
                </div>
              </footer>
            </article>
          );
        })}
      </div>

      {orders.some(o => o.orderStatus === 'PENDING' && o.paymentStatus === 'PENDING') && (
        <p className="orders-note">
          Payment is simulated in this project: &ldquo;Pay now&rdquo; marks the order as paid.
        </p>
      )}
    </main>
  );
}
