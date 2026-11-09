import React, { useState } from 'react';
import { useCart } from '../context/CartContext';
import { money } from '../data/products';
import { photoFor } from '../utils/productImages';
import api from '../services/api';
import '../styles/cart.css';

export default function Cart({ onContinueShopping, onCheckout }) {
  const {
    cart,
    total,
    updateQuantity,
    removeFromCart,
    clearCart
  } = useCart();

  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [orderType, setOrderType] = useState('CLICK_AND_COLLECT');
  const [deliveryArea, setDeliveryArea] = useState('Cape Town CBD');
  const [streetAddress, setStreetAddress] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [createdOrder, setCreatedOrder] = useState(null);

  async function submitOrder(event) {
    event.preventDefault();
    setError('');
    setSuccess('');

    const storedUser =
      localStorage.getItem('pharmaflow_user') ||
      sessionStorage.getItem('pharmaflow_user');

    if (!storedUser) {
      setError('Please log in before placing an order.');
      return;
    }

    let user;

    try {
      user = JSON.parse(storedUser);
    } catch {
      setError('Your login session is invalid. Please log in again.');
      return;
    }

    if (user?.id === undefined || user?.id === null) {
      setError('Your account ID is missing. Please log in again.');
      return;
    }

    if (user.role && user.role !== 'CUSTOMER') {
      setError('Only customer accounts can place orders.');
      return;
    }

    if (cart.length === 0) {
      setError('Your cart is empty.');
      return;
    }

    const unavailable = cart.find(item => Number(item.stock) <= 0 || item.quantity > Number(item.stock));
    if (unavailable) {
      setError(`${unavailable.name} is no longer available in the requested quantity. Update your cart and try again.`);
      return;
    }

    if (orderType === 'DELIVERY' && (!deliveryArea.trim() || !streetAddress.trim())) {
      setError('Please choose a delivery area and enter your street address.');
      return;
    }

    const items = cart.map(item => ({
      productId: String(item.id),
      quantity: item.quantity,
      prescriptionId: item.prescriptionId || null
    }));

    const requestBody = {
      customerId: String(user.id),
      orderType,
      address: orderType === 'DELIVERY'
        ? `${streetAddress.trim()}, ${deliveryArea}, Cape Town, South Africa`
        : null,
      items
    };

    setLoading(true);

    try {
      const order = await api.post('/order/create', requestBody);

      setCreatedOrder(order);
      setSuccess(
        `Your order was created successfully${
          order?.orderNumber ? `: ${order.orderNumber}` : '.'
        }`
      );

      clearCart();
      setCheckoutOpen(false);
      setStreetAddress('');
      setDeliveryArea('Cape Town CBD');
    } catch (err) {
      const message = String(err?.message || '').toLowerCase();
      const status = err?.status || err?.response?.status;

      const prescriptionIssue =
        message.includes('prescription') &&
        (
          message.includes('approv') ||
          message.includes('verif') ||
          message.includes('pending') ||
          message.includes('refill') ||
          message.includes('valid')
        );

      if (prescriptionIssue) {
        setError(
          'We’re unable to complete your order yet. Please visit My Prescriptions to check your prescription status. You can order this medicine once the required verification is complete.'
        );
      } else if (status === 409) {
        setError(
          'We couldn’t complete your order at this time. Please check your order details or try again later.'
        );
      } else {
        setError(
          'We couldn’t place your order. Please check your details and try again.'
        );
      }
    } finally {
      setLoading(false);
    }
  }

  if (cart.length === 0) {
    return (
      <main className="cart-page">
        <h1>Your Shopping Cart</h1>

        {success ? (
          <section className="cart-summary" role="status">
            <h2>Order placed successfully</h2>
            <p>{success}</p>

            {createdOrder?.orderStatus && (
              <p>Order status: {createdOrder.orderStatus}</p>
            )}

            {createdOrder?.paymentStatus && (
              <p>Payment status: {createdOrder.paymentStatus}</p>
            )}

            <button
              className="hp-btn hp-btn-primary"
              onClick={() =>
                window.dispatchEvent(
                  new CustomEvent('pharmaflow-open-orders')
                )
              }
            >
              View My Orders
            </button>{' '}

            <button
              className="hp-btn hp-btn-ghost"
              onClick={onContinueShopping}
            >
              Continue Shopping
            </button>
          </section>
        ) : (
          <>
            <p>Your cart is currently empty.</p>

            <button
              className="hp-btn hp-btn-primary"
              onClick={onContinueShopping}
            >
              Browse Products
            </button>
          </>
        )}

        {error && (
          <div
            role="alert"
            className="cart-message cart-message-error"
          >
            {error}
          </div>
        )}
      </main>
    );
  }

  return (
    <main className="cart-page">
      <h1>Your Shopping Cart</h1>

      {error && (
        <div
          role="alert"
          className="cart-message cart-message-error"
        >
          {error}
        </div>
      )}

      {success && (
        <div
          role="status"
          className="cart-message cart-message-success"
        >
          {success}
        </div>
      )}

      <div className="cart-items">
        {cart.map(item => (
          <article className="cart-item" key={item.id}>
            <div className="cart-item-icon">
              <img
                src={item.image || photoFor(item)}
                alt={item.name}
                onError={event => { event.currentTarget.style.display = 'none'; }}
              />
            </div>

            <div className="cart-item-details">
              <h2>{item.name}</h2>
              <p>{money(item.price)} each</p>

              {item.rx && (
                <p className="cart-rx-notice">
                  Prescription required before dispensing.
                </p>
              )}

              <div className="cart-quantity">
                <button
                  type="button"
                  aria-label={`Decrease ${item.name} quantity`}
                  disabled={item.quantity <= 1 || loading}
                  onClick={() =>
                    updateQuantity(item.id, item.quantity - 1)
                  }
                >
                  −
                </button>

                <span>{item.quantity}</span>

                <button
                  type="button"
                  aria-label={`Increase ${item.name} quantity`}
                  disabled={
                    item.quantity >= item.stock || loading
                  }
                  onClick={() =>
                    updateQuantity(item.id, item.quantity + 1)
                  }
                >
                  +
                </button>
              </div>
            </div>

            <div className="cart-item-actions">
              <strong>
                {money(item.price * item.quantity)}
              </strong>

              <button
                type="button"
                className="cart-remove"
                disabled={loading}
                onClick={() => removeFromCart(item.id)}
              >
                Remove
              </button>
            </div>
          </article>
        ))}
      </div>

      <section className="cart-summary">
        <div>
          <span>Subtotal</span>
          <strong>{money(total)}</strong>
        </div>

        <p>
          Delivery or collection details will be confirmed at checkout.
        </p>

        {!checkoutOpen ? (
          <>
            <button
              className="hp-btn hp-btn-primary hp-btn-lg"
              type="button"
              onClick={() => {
                setError('');
                const savedUser = localStorage.getItem('pharmaflow_user') || sessionStorage.getItem('pharmaflow_user');
                if (!savedUser) {
                  setError('Please sign in before checkout. You can keep your cart and return after signing in.');
                  window.dispatchEvent(new Event('pharmaflow-require-customer-login'));
                  return;
                }
                setCheckoutOpen(true);
              }}
            >
              Proceed to Checkout
            </button>

            <button
              className="hp-btn hp-btn-small"
              type="button"
              onClick={onContinueShopping}
            >
              Continue Shopping
            </button>
          </>
        ) : (
          <form
            className="cart-checkout-form"
            onSubmit={submitOrder}
          >
            <h2>Checkout</h2>

            <label htmlFor="orderType">
              Fulfilment method
            </label>

            <select
              id="orderType"
              value={orderType}
              onChange={event => setOrderType(event.target.value)}
              disabled={loading}
            >
              <option value="CLICK_AND_COLLECT">
                Collect from pharmacy
              </option>
              <option value="DELIVERY">
                Delivery
              </option>
            </select>

            {orderType === 'DELIVERY' && (
              <>
                <label htmlFor="deliveryArea">
                  Cape Town delivery area
                </label>

                <select
                  id="deliveryArea"
                  value={deliveryArea}
                  onChange={event => setDeliveryArea(event.target.value)}
                  required
                  disabled={loading}
                >
                  <option value="Cape Town CBD">Cape Town CBD</option>
                  <option value="Bellville">Bellville</option>
                  <option value="Claremont">Claremont</option>
                  <option value="Goodwood">Goodwood</option>
                  <option value="Khayelitsha">Khayelitsha</option>
                  <option value="Kuils River">Kuils River</option>
                  <option value="Milnerton">Milnerton</option>
                  <option value="Observatory">Observatory</option>
                  <option value="Parow">Parow</option>
                  <option value="Sea Point">Sea Point</option>
                  <option value="Somerset West">Somerset West</option>
                  <option value="Southern Suburbs">Southern Suburbs</option>
                  <option value="Table View">Table View</option>
                  <option value="Woodstock">Woodstock</option>
                </select>

                <label htmlFor="deliveryStreetAddress">
                  Street address
                </label>

                <textarea
                  id="deliveryStreetAddress"
                  value={streetAddress}
                  onChange={event => setStreetAddress(event.target.value)}
                  placeholder="Enter house/building number and street name"
                  rows={3}
                  required
                  disabled={loading}
                />

                <p className="cart-delivery-hint">
                  Your courier will receive this address together with the selected delivery area.
                </p>
              </>
            )}

            {cart.some(item => item.rx) && (
              <p className="cart-rx-notice">
                Your cart contains prescription-only medicine.
                Our pharmacy team must verify the required
                prescription before the medicine can be dispensed.
              </p>
            )}

            <button
              className="hp-btn hp-btn-primary hp-btn-lg"
              type="submit"
              disabled={loading}
            >
              {loading
                ? 'Placing order…'
                : `Place Order · ${money(total)}`}
            </button>

            <button
              className="hp-btn hp-btn-small"
              type="button"
              disabled={loading}
              onClick={() => {
                setCheckoutOpen(false);
                setError('');
              }}
            >
              Back to Cart
            </button>
          </form>
        )}
      </section>
    </main>
  );
}