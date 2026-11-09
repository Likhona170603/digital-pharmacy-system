import React, { useCallback, useEffect, useMemo, useState } from 'react';
import api from '../services/api';
import { openPrescriptionFile } from '../services/prescriptionFiles';
import { money } from '../data/products';
import useInventory from '../hooks/useInventory';
import '../styles/pharmacist.css';

const STATUS = {
  PENDING: 'Pending review',
  APPROVED: 'Verified',
  REJECTED: 'Rejected',
  NEEDS_CLARIFICATION: 'Needs information'
};

const formatDate = value => {
  if (!value) return '—';

  try {
    const date = Array.isArray(value)
      ? new Date(value[0], (value[1] || 1) - 1, value[2] || 1)
      : new Date(value);

    return Number.isNaN(date.getTime())
      ? String(value)
      : date.toLocaleString([], {
          dateStyle: 'medium',
          timeStyle: 'short'
        });
  } catch {
    return String(value);
  }
};

const getPrescriptionStatus = value =>
  STATUS[String(value || '').toUpperCase()] || String(value || 'Unknown');

const getPharmacistIdentity = () => {
  const keys = [
    'pharmaflow_user',
    'pharmaflow_user_data',
    'currentUser',
    'user'
  ];

  for (const key of keys) {
    try {
      const raw =
        localStorage.getItem(key) || sessionStorage.getItem(key);

      if (!raw) continue;

      const user = JSON.parse(raw);
      const identity =
        user?.username ||
        user?.email ||
        user?.userId ||
        user?.id;

      if (identity) return String(identity);
    } catch {
      const value =
        localStorage.getItem(key) || sessionStorage.getItem(key);

      if (value && !value.startsWith('{')) return value;
    }
  }

  return 'Pharmacist';
};

export default function PharmacistDashboard({ onNotify, onHome }) {
  const [tab, setTab] = useState('Overview');

  const [prescriptions, setPrescriptions] = useState([]);
  const [prescriptionsLoading, setPrescriptionsLoading] = useState(false);
  const [prescriptionsError, setPrescriptionsError] = useState('');
  const [busyPrescription, setBusyPrescription] = useState('');

  const [orders, setOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [ordersError, setOrdersError] = useState('');
  const [busyOrder, setBusyOrder] = useState(null);

  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('All');
  const [selected, setSelected] = useState(null);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [reviewNote, setReviewNote] = useState('');

  const {
    inventory,
    loading: inventoryLoading,
    error: inventoryError,
    reload: reloadInventory
  } = useInventory();

  const [stockItem, setStockItem] = useState(null);
  const [stockValue, setStockValue] = useState('');
  const [stockError, setStockError] = useState('');
  const [stockBusy, setStockBusy] = useState(false);

  const notify = text => onNotify?.(text);
  const viewPrescriptionFile = async prescription => {
    try {
      await openPrescriptionFile(prescription?.prescriptionId || prescription?.id);
    } catch (error) {
      notify(error.message || 'Could not open this prescription file.');
    }
  };

  const lowStock = useMemo(
    () =>
      inventory
        .filter(product => product.active && product.stock <= product.reorder)
        .sort((a, b) => a.stock - b.stock),
    [inventory]
  );

  const pending = useMemo(
    () =>
      prescriptions.filter(prescription =>
        ['PENDING', 'NEEDS_CLARIFICATION'].includes(
          String(prescription.verificationStatus || '').toUpperCase()
        )
      ).length,
    [prescriptions]
  );

  const visiblePrescriptions = useMemo(() => {
    return prescriptions.filter(prescription => {
      const status = getPrescriptionStatus(
        prescription.verificationStatus
      );

      const searchText = [
        prescription.prescriptionId,
        prescription.patientId,
        prescription.productId,
        prescription.issuedDate,
        prescription.filePath,
        status
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();

      const matchesQuery = searchText.includes(query.toLowerCase());
      const matchesFilter = filter === 'All' || status === filter;

      return matchesQuery && matchesFilter;
    });
  }, [prescriptions, query, filter]);

  const loadPrescriptions = useCallback(async () => {
    setPrescriptionsLoading(true);
    setPrescriptionsError('');

    try {
      const data = await api.get('/prescription/pending');
      setPrescriptions(Array.isArray(data) ? data : []);
    } catch (err) {
      setPrescriptionsError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to view prescriptions. Log in with a pharmacist account.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not load prescriptions.'
      );
    } finally {
      setPrescriptionsLoading(false);
    }
  }, []);

  const loadOrders = useCallback(async () => {
    setOrdersLoading(true);
    setOrdersError('');

    try {
      const data = await api.get('/order/getall');
      const records = Array.isArray(data) ? data : [];

      let usersById = {};

      try {
        const users = await api.get('/user/getAll');

        usersById = Object.fromEntries(
          (Array.isArray(users) ? users : []).map(user => [
            String(user.id),
            user
          ])
        );
      } catch {
        usersById = {};
      }

      setOrders(
        records.map(order => ({
          ...order,
          id: order.orderNumber ?? order.id,
          customer:
            usersById[String(order.customerId)]?.fullName ||
            `Customer #${order.customerId ?? '?'}`,
          customerEmail:
            usersById[String(order.customerId)]?.email || '',
          orderItems: Array.isArray(order.items) ? order.items : [],
          items: Array.isArray(order.items)
            ? order.items.reduce(
                (sum, item) => sum + Number(item.orderQty || 0),
                0
              )
            : 0,
          placed: order.createdAt
            ? formatDate(order.createdAt)
            : '—',
          status: order.orderStatus || 'UNKNOWN'
        }))
      );
    } catch (err) {
      setOrdersError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to view all orders. Check that you are logged in with a pharmacist account.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not load orders.'
      );
    } finally {
      setOrdersLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPrescriptions();
    loadOrders();
  }, [loadPrescriptions, loadOrders]);

  const openStock = item => {
    setStockItem(item);
    setStockValue(String(item.stock));
    setStockError('');
  };

  const saveStock = async event => {
    event.preventDefault();

    const quantity = Number(stockValue);

    if (
      stockValue.trim() === '' ||
      !Number.isInteger(quantity) ||
      quantity < 0
    ) {
      setStockError('Stock must be a whole number of 0 or more.');
      return;
    }

    setStockBusy(true);
    setStockError('');

    try {
      await api.patch(
        `/product/${encodeURIComponent(stockItem.id)}/stock`,
        { quantity }
      );

      notify(`Stock for ${stockItem.name} is now ${quantity}.`);
      setStockItem(null);
      await reloadInventory();
    } catch (err) {
      setStockError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to update stock. Log in with a pharmacist account.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not update the stock.'
      );
    } finally {
      setStockBusy(false);
    }
  };

  const stockLabel = product =>
    product.stock <= 0
      ? 'Out of stock'
      : product.stock <= product.reorder
        ? 'Low stock'
        : 'In stock';

  const review = async (item, action) => {
    const prescriptionId = item?.prescriptionId;

    if (!prescriptionId) {
      notify('This prescription has no ID and cannot be updated.');
      return;
    }

    const currentStatus = String(
      item.verificationStatus || ''
    ).toUpperCase();

    if (
      !['PENDING', 'NEEDS_CLARIFICATION'].includes(currentStatus)
    ) {
      notify('This prescription is no longer awaiting review.');
      return;
    }

    if (
      ['reject', 'clarify'].includes(action) &&
      !reviewNote.trim()
    ) {
      notify(
        action === 'reject'
          ? 'Enter a reason before rejecting the prescription.'
          : 'Enter a reason explaining what information is needed.'
      );
      return;
    }

    setBusyPrescription(prescriptionId);
    setPrescriptionsError('');

    try {
      const id = encodeURIComponent(String(prescriptionId));
      const pharmacist = encodeURIComponent(getPharmacistIdentity());

      let endpoint;

      if (action === 'approve') {
        endpoint =
          `/prescription/${id}/approve` +
          `?refills=1&verifiedBy=${pharmacist}`;
      } else {
        const reason = encodeURIComponent(reviewNote.trim());
        const actionPath = action === 'reject' ? 'reject' : 'clarify';

        endpoint =
          `/prescription/${id}/${actionPath}` +
          `?reason=${reason}&verifiedBy=${pharmacist}`;
      }

      await api.put(endpoint, {});

      const successMessage = {
        approve: 'Prescription approved.',
        reject: 'Prescription rejected.',
        clarify: 'Clarification requested.'
      };

      notify(`${prescriptionId}: ${successMessage[action]}`);

      setSelected(null);
      setReviewNote('');

      await loadPrescriptions();
    } catch (err) {
      const message =
        err.status === 401 || err.status === 403
          ? 'You are not authorised to review prescriptions. Check your pharmacist login.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'The prescription decision could not be saved.';

      setPrescriptionsError(message);
      notify(message);
    } finally {
      setBusyPrescription('');
    }
  };

  const packOrder = async order => {
    const id = order.orderNumber ?? order.id;

    if (!id) return;

    setBusyOrder(id);
    setOrdersError('');

    try {
      await api.post(`/order/${encodeURIComponent(String(id))}/pack`);

      notify(`Order ${id} marked as packed.`);
      await loadOrders();
    } catch (err) {
      setOrdersError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to pack orders. Check the backend pharmacist permissions.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not update this order.'
      );
    } finally {
      setBusyOrder(null);
    }
  };

  const menu = [
    'Overview',
    'Prescription Queue',
    'Prepare Orders',
    'Product Catalogue',
    'Patient Records'
  ];

  const badge = status => {
    const tone = [
      'Verified',
      'APPROVED',
      'Ready to pack',
      'Ready for collection',
      'Dispatched',
      'In stock',
      'PAID'
    ].includes(status)
      ? 'good'
      : [
          'Needs information',
          'NEEDS_CLARIFICATION',
          'Awaiting verification',
          'Low stock'
        ].includes(status)
        ? 'warn'
        : ['Out of stock', 'Rejected', 'REJECTED'].includes(status)
          ? 'bad'
          : 'info';

    return <span className={`ph-badge ${tone}`}>{status}</span>;
  };

  return (
    <div className="ph-workspace">
      <header className="ph-topbar">
        <button
          className="ph-brand"
          onClick={onHome}
          aria-label="Return to public website"
        >
          <span className="ph-logo">✚</span>
          <span>
            Pharma<span>Flow</span>
            <small>PHARMACY WORKSPACE</small>
          </span>
        </button>

        <div className="ph-top-right">
          <span className="ph-demo-tag">PHARMACY WORKSPACE</span>
          <span className="ph-top-name">
            <span className="ph-avatar">PH</span>
            <span>
              Pharmacist account
              <small>Pharmacy team</small>
            </span>
          </span>
          <button className="ph-out" onClick={onHome}>
            Exit workspace
          </button>
        </div>
      </header>

      <div className="ph-shell">
        <aside className="ph-sidebar">
          <div className="ph-side-caption">WORKSPACE</div>

          {menu.map((item, index) => (
            <button
              key={item}
              className={`ph-side-link ${tab === item ? 'active' : ''}`}
              onClick={() => {
                setTab(item);
                setQuery('');
              }}
            >
              <span>{['▦', '▧', '▣', '▤', '♙'][index]}</span>
              {item}
              {item === 'Prescription Queue' && pending > 0 && (
                <b>{pending}</b>
              )}
            </button>
          ))}

          <div className="ph-side-bottom">
            <div className="ph-help-icon">?</div>
            <strong>Need help?</strong>
            <p>
              Check pharmacy workflow guidance or contact your system
              administrator.
            </p>
            <button
              onClick={() =>
                notify('Help centre will be connected in a future version.')
              }
            >
              Open help centre ↗
            </button>
          </div>

          <div className="ph-sidebar-foot">
            PharmaFlow v1.0 · Prototype
          </div>
        </aside>

        <main className="ph-main">
          {tab === 'Overview' && (
            <Overview
              pending={pending}
              prescriptions={prescriptions}
              prescriptionsLoading={prescriptionsLoading}
              orders={orders}
              lowStock={lowStock}
              badge={badge}
              setTab={setTab}
            />
          )}

          {tab === 'Prescription Queue' && (
            <section>
              <PageTitle
                title="Prescription queue"
                subtitle="Review incoming prescriptions and record the pharmacist's decision."
              />

              <div className="ph-info-banner">
                <span>ⓘ</span>
                <div>
                  <strong>Patient safety comes first</strong>
                  <p>
                    Verify the prescription, patient details, medicine,
                    dosage and validity before approving. Prescription
                    details are loaded from the backend.
                  </p>
                </div>
              </div>

              {prescriptionsError && (
                <div className="ph-info-banner" role="alert">
                  <span>!</span>
                  <div>
                    <strong>Prescription queue error</strong>
                    <p>{prescriptionsError}</p>
                    <button
                      className="ph-btn ph-btn-outline"
                      onClick={loadPrescriptions}
                      disabled={prescriptionsLoading}
                    >
                      Try again
                    </button>
                  </div>
                </div>
              )}

              <div className="ph-toolbar">
                <input
                  value={query}
                  onChange={event => setQuery(event.target.value)}
                  placeholder="Search reference, patient ID or product ID…"
                  aria-label="Search prescriptions"
                />

                <select
                  value={filter}
                  onChange={event => setFilter(event.target.value)}
                  aria-label="Filter prescriptions"
                >
                  {[
                    'All',
                    'Pending review',
                    'Needs information',
                    'Verified',
                    'Rejected'
                  ].map(status => (
                    <option key={status}>{status}</option>
                  ))}
                </select>

                <button
                  className="ph-btn ph-btn-outline"
                  onClick={loadPrescriptions}
                  disabled={prescriptionsLoading}
                >
                  {prescriptionsLoading ? 'Refreshing…' : 'Refresh'}
                </button>
              </div>

              <div className="ph-panel">
                <div className="ph-panel-heading">
                  <div>
                    <h2>Incoming prescriptions</h2>
                    <p>
                      {visiblePrescriptions.length} records shown
                    </p>
                  </div>
                  <span className="ph-soft-label">
                    {pending} awaiting action
                  </span>
                </div>

                <div className="ph-table-wrap">
                  {prescriptionsLoading && prescriptions.length === 0 ? (
                    <div className="ph-empty">
                      Loading prescriptions from the backend…
                    </div>
                  ) : (
                    <table>
                      <thead>
                        <tr>
                          <th>Reference</th>
                          <th>Patient ID</th>
                          <th>Product ID</th>
                          <th>Issued date</th>
                          <th>Status</th>
                          <th>Prescription file</th>
                          <th></th>
                        </tr>
                      </thead>

                      <tbody>
                        {visiblePrescriptions.map(prescription => {
                          const status = getPrescriptionStatus(
                            prescription.verificationStatus
                          );

                          return (
                            <tr key={prescription.prescriptionId}>
                              <td>
                                <strong>
                                  {prescription.prescriptionId}
                                </strong>
                              </td>

                              <td>{prescription.patientId || '—'}</td>

                              <td>{prescription.productId || '—'}</td>

                              <td>
                                {formatDate(prescription.issuedDate)}
                              </td>

                              <td>{badge(status)}</td>

                              <td>
                                {prescription.filePath ? (
                                  <button type="button" className="ph-link" onClick={() => viewPrescriptionFile(prescription)}>View file ↗</button>
                                ) : (
                                  <span className="ph-muted">No file path</span>
                                )}
                              </td>

                              <td>
                                <button
                                  className="ph-btn ph-btn-small"
                                  onClick={() => {
                                    setSelected(prescription);
                                    setReviewNote(
                                      prescription.rejectionReason || ''
                                    );
                                  }}
                                  disabled={
                                    busyPrescription ===
                                    prescription.prescriptionId
                                  }
                                >
                                  Review
                                </button>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  )}

                  {!prescriptionsLoading &&
                    visiblePrescriptions.length === 0 && (
                      <div className="ph-empty">
                        {prescriptionsError
                          ? 'Prescriptions could not be loaded. Try refreshing.'
                          : 'No prescriptions match these filters.'}
                      </div>
                    )}
                </div>
              </div>
            </section>
          )}

          {tab === 'Prepare Orders' && (
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
                    Confirm payment, medicine, strength, quantity and
                    prescription approval before packing an order.
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
                  <div className="ph-empty">
                    Loading orders from the backend…
                  </div>
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
                            order.orderStatus ||
                            order.status ||
                            'UNKNOWN';
                          const paid = order.paymentStatus === 'PAID';
                          const canPack =
                            paid && status === 'PROCESSING';

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
                              <td>
                                {badge(status.replaceAll('_', ' '))}
                              </td>
                              <td>
                                {badge(order.paymentStatus || 'UNKNOWN')}
                              </td>

                              <td>
                                <button
                                  className="ph-btn ph-btn-small"
                                  onClick={() => setSelectedOrder(order)}
                                >
                                  View items
                                </button>
                                {canPack ? <span className="ph-muted" style={{display:'block',marginTop:5}}>Review before packing</span> : (
                                  <span className="ph-muted" style={{display:'block',marginTop:5}}>
                                    {status === 'CANCELLED' ? 'Cancelled' : ['PACKED','READY_FOR_COLLECTION','SHIPPED','DELIVERED','COLLECTED'].includes(status) ? 'No action needed' : !paid ? 'Awaiting payment' : 'Review status'}
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
                Order data and packing actions use the backend API. Always
                confirm prescription approval before dispensing.
              </p>
            </section>
          )}

          {tab === 'Product Catalogue' && (
            <section>
              <PageTitle
                title="Product catalogue"
                subtitle="Review stock availability and update stock levels after counts or deliveries."
              />

              {inventoryError && (
                <div className="ph-info-banner" role="alert">
                  <span>!</span>
                  <div>
                    <strong>Products could not be loaded</strong>
                    <p>
                      {inventoryError.status === 401 ||
                      inventoryError.status === 403
                        ? 'You are not authorised to view the full catalogue. Log in with a pharmacist account.'
                        : inventoryError instanceof TypeError
                          ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
                          : inventoryError.message ||
                            'Could not load products.'}
                    </p>
                  </div>
                </div>
              )}

              <div className="ph-toolbar">
                <input
                  value={query}
                  onChange={event => setQuery(event.target.value)}
                  placeholder="Search product, SKU or category…"
                  aria-label="Search products"
                />

                <button
                  className="ph-btn ph-btn-outline"
                  onClick={reloadInventory}
                  disabled={inventoryLoading}
                >
                  {inventoryLoading ? 'Refreshing…' : 'Refresh'}
                </button>

                <span className="ph-soft-label">
                  {inventory.length} products
                </span>
              </div>

              <div className="ph-panel">
                <div className="ph-table-wrap">
                  {inventoryLoading && inventory.length === 0 ? (
                    <div className="ph-empty">
                      Loading products from the backend…
                    </div>
                  ) : (
                    <table>
                      <thead>
                        <tr>
                          <th>Product</th>
                          <th>Category</th>
                          <th>Schedule</th>
                          <th>Price</th>
                          <th>Stock</th>
                          <th>Availability</th>
                          <th>Action</th>
                        </tr>
                      </thead>

                      <tbody>
                        {inventory
                          .filter(product =>
                            `${product.name} ${product.sku} ${product.category}`
                              .toLowerCase()
                              .includes(query.toLowerCase())
                          )
                          .map(product => (
                            <tr key={product.id}>
                              <td>
                                <span className="ph-product-name">
                                  <img className="ph-product-thumb" src={`/images/products/${String(product.id || '').toUpperCase()}.webp`} alt="" onError={event => { event.currentTarget.style.display = 'none'; }} />
                                  <span>
                                    <strong>{product.name}</strong>
                                    <small>{product.sku}</small>
                                  </span>
                                </span>
                              </td>

                              <td>{product.category}</td>
                              <td>{product.schedule}</td>
                              <td>{money(product.price)}</td>
                              <td>{product.stock}</td>

                              <td>
                                {product.active
                                  ? badge(stockLabel(product))
                                  : badge('Inactive')}
                              </td>

                              <td>
                                <button
                                  className="ph-btn ph-btn-small"
                                  onClick={() => openStock(product)}
                                >
                                  Update stock
                                </button>
                              </td>
                            </tr>
                          ))}
                      </tbody>
                    </table>
                  )}

                  {!inventoryLoading &&
                    !inventoryError &&
                    inventory.length === 0 && (
                      <div className="ph-empty">
                        No products were returned by the backend.
                      </div>
                    )}
                </div>
              </div>
            </section>
          )}

          {tab === 'Patient Records' && (
            <section>
              <PageTitle
                title="Patient records"
                subtitle="Restricted area for authorised pharmacy personnel."
              />

              <div className="ph-info-banner">
                <span>🔒</span>
                <div>
                  <strong>Privacy and confidentiality</strong>
                  <p>
                    Real patient records must only be displayed after secure
                    authentication and permission checks. This backend
                    integration does not yet provide a confirmed patient
                    records endpoint.
                  </p>
                </div>
              </div>

              <div className="ph-panel">
                <div className="ph-panel-heading">
                  <div>
                    <h2>Patient records</h2>
                    <p>Patient details are not connected to this screen.</p>
                  </div>
                </div>

                <div className="ph-empty">
                  Patient records will appear here when a secure patient
                  records API is implemented.
                </div>
              </div>
            </section>
          )}
        </main>
      </div>

      {selectedOrder && (
        <div className="ph-modal-backdrop" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget) setSelectedOrder(null); }}>
          <section className="ph-review-modal" role="dialog" aria-modal="true" aria-labelledby="ph-order-items-title">
            <button className="ph-modal-close" onClick={() => setSelectedOrder(null)} aria-label="Close order details">×</button>
            <span className="ph-badge info">Order details</span>
            <h2 id="ph-order-items-title">Order {selectedOrder.orderNumber ?? selectedOrder.id}</h2>
            <p className="ph-muted">Review each item and quantity before packing this order.</p>
            <div className="ph-table-wrap">
              <table>
                <thead><tr><th>Item</th><th>Quantity</th><th>Unit price</th></tr></thead>
                <tbody>
                  {(Array.isArray(selectedOrder.orderItems) ? selectedOrder.orderItems : []).map((line, index) => (
                    <tr key={line.id ?? line.orderItemId ?? index}>
                      <td><strong>{line.product?.productName || line.product?.name || line.productName || line.name || `Product ${line.productId ?? ''}`}</strong></td>
                      <td>{line.orderQty ?? line.quantity ?? 1}</td>
                      <td>{line.unitPrice != null ? money(line.unitPrice) : line.price != null ? money(line.price) : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {(!Array.isArray(selectedOrder.orderItems) || selectedOrder.orderItems.length === 0) && <div className="ph-empty">No item lines were returned for this order. Check the order API response before packing.</div>}
            </div>
            <div className="ph-modal-actions">
              <button className="ph-btn" onClick={() => setSelectedOrder(null)}>Close</button>
              {String(selectedOrder.paymentStatus || '').toUpperCase() === 'PAID' && ['PROCESSING','PENDING'].includes(String(selectedOrder.orderStatus || selectedOrder.status || '').toUpperCase()) && (
                <button className="ph-btn ph-btn-primary" disabled={busyOrder === (selectedOrder.orderNumber ?? selectedOrder.id)} onClick={async () => { await packOrder(selectedOrder); setSelectedOrder(null); }}>
                  {busyOrder === (selectedOrder.orderNumber ?? selectedOrder.id) ? 'Packing…' : 'Mark as Packed'}
                </button>
              )}
            </div>
          </section>
        </div>
      )}

            {stockItem && (
        <div
          className="ph-modal-backdrop"
          onMouseDown={event =>
            event.target === event.currentTarget &&
            !stockBusy &&
            setStockItem(null)
          }
        >
          <form
            className="ph-review-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="stock-title"
            onSubmit={saveStock}
          >
            <button
              type="button"
              className="ph-modal-close"
              onClick={() => setStockItem(null)}
              aria-label="Close"
              disabled={stockBusy}
            >
              ×
            </button>

            <span className="ph-soft-label">UPDATE STOCK</span>
            <h2 id="stock-title">{stockItem.name}</h2>
            <p className="ph-muted">
              {stockItem.sku} · Currently {stockItem.stock} on hand
            </p>

            <label className="ph-note-label" htmlFor="stock-value">
              New stock quantity
            </label>

            <input
              id="stock-value"
              className="ph-number-input"
              type="number"
              min="0"
              step="1"
              value={stockValue}
              onChange={event => setStockValue(event.target.value)}
              autoFocus
            />

            {stockError && (
              <p className="ph-form-error" role="alert">
                {stockError}
              </p>
            )}

            <div className="ph-modal-actions">
              <button
                type="button"
                className="ph-btn ph-btn-outline"
                onClick={() => setStockItem(null)}
                disabled={stockBusy}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="ph-btn ph-btn-primary"
                disabled={stockBusy}
              >
                {stockBusy ? 'Saving…' : 'Save stock'}
              </button>
            </div>

            <p className="ph-modal-footnote">
              This sets the exact quantity on hand and is recorded in the
              audit log.
            </p>
          </form>
        </div>
      )}

      {selected && (
        <div
          className="ph-modal-backdrop"
          onMouseDown={event =>
            event.target === event.currentTarget &&
            !busyPrescription &&
            setSelected(null)
          }
        >
          <section
            className="ph-review-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="review-title"
          >
            <button
              className="ph-modal-close"
              onClick={() => setSelected(null)}
              aria-label="Close"
              disabled={Boolean(busyPrescription)}
            >
              ×
            </button>

            <span className="ph-soft-label">PRESCRIPTION REVIEW</span>

            <h2 id="review-title">
              {selected.prescriptionId}
            </h2>

            <p className="ph-muted">
              Issued {formatDate(selected.issuedDate)}
            </p>

            <div className="ph-review-details">
              <div>
                <small>Patient ID</small>
                <strong>{selected.patientId || '—'}</strong>
              </div>

              <div>
                <small>Product ID</small>
                <strong>{selected.productId || '—'}</strong>
              </div>

              <div>
                <small>Current status</small>
                <strong>
                  {getPrescriptionStatus(selected.verificationStatus)}
                </strong>
              </div>

              <div>
                <small>Previously verified by</small>
                <strong>{selected.verifiedBy || 'Not recorded'}</strong>
              </div>

              <div>
                <small>Verification date</small>
                <strong>{formatDate(selected.verifiedAt)}</strong>
              </div>

              <div>
                <small>Prescription file</small>
                {selected.filePath ? (
                  <button type="button" className="ph-link" onClick={() => viewPrescriptionFile(selected)}>Open prescription ↗</button>
                ) : (
                  <strong>Not provided</strong>
                )}
              </div>
            </div>

            {selected.rejectionReason && (
              <div className="ph-info-banner">
                <span>ⓘ</span>
                <div>
                  <strong>Previous review reason</strong>
                  <p>{selected.rejectionReason}</p>
                </div>
              </div>
            )}

            <div className="ph-info-banner">
              <span>✓</span>
              <div>
                <strong>Review before recording a decision</strong>
                <p>
                  Inspect the prescription and verify the relevant details.
                  A product ID alone does not establish the medicine,
                  dosage, or clinical appropriateness.
                </p>
              </div>
            </div>

            <label className="ph-note-label" htmlFor="review-note">
              Reason for rejection or clarification
            </label>

            <textarea
              id="review-note"
              value={reviewNote}
              onChange={event => setReviewNote(event.target.value)}
              placeholder="Required when rejecting or requesting clarification…"
              rows="3"
              disabled={Boolean(busyPrescription)}
            />

            <div className="ph-modal-actions">
              <button
                type="button"
                className="ph-btn ph-btn-danger"
                onClick={() => review(selected, 'reject')}
                disabled={Boolean(busyPrescription)}
              >
                {busyPrescription === selected.prescriptionId
                  ? 'Saving…'
                  : 'Reject'}
              </button>

              <button
                type="button"
                className="ph-btn ph-btn-outline"
                onClick={() => review(selected, 'clarify')}
                disabled={Boolean(busyPrescription)}
              >
                Request clarification
              </button>

              <button
                type="button"
                className="ph-btn ph-btn-primary"
                onClick={() => review(selected, 'approve')}
                disabled={Boolean(busyPrescription)}
              >
                Approve prescription
              </button>
            </div>

            <p className="ph-modal-footnote">
              Decisions are submitted to the Spring Boot backend. Approval
              records one refill because the controller defaults to one.
            </p>
          </section>
        </div>
      )}
    </div>
  );
}

function PageTitle({ title, subtitle }) {
  return (
    <div className="ph-page-title">
      <div>
        <div className="ph-kicker">PHARMACIST WORKSPACE</div>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>
      <span className="ph-status-dot">
        <i /> Pharmacy workspace
      </span>
    </div>
  );
}

function Overview({
  pending,
  prescriptions,
  prescriptionsLoading,
  orders,
  lowStock,
  badge,
  setTab
}) {
  const recentPrescriptions = prescriptions.slice(0, 3);

  return (
    <section>
      <PageTitle
        title="Good day, Pharmacist"
        subtitle="Here is your pharmacy operations summary for today."
      />

      <div className="ph-welcome">
        <div>
          <span className="ph-welcome-tag">TODAY'S WORKSPACE</span>
          <h2>Care starts with every detail.</h2>
          <p>
            Review prescriptions, prepare orders and keep medicine
            availability visible to the team.
          </p>
        </div>
        <div className="ph-welcome-symbol">✚</div>
      </div>

      <div className="ph-stat-grid">
        <Stat
          icon="▧"
          label="Pending prescriptions"
          value={pending}
          note="Need pharmacist attention"
          tone="blue"
          onClick={() => setTab('Prescription Queue')}
        />

        <Stat
          icon="▣"
          label="Orders to prepare"
          value={
            orders.filter(
              order =>
                ![
                  'SHIPPED',
                  'DELIVERED',
                  'COLLECTED',
                  'CANCELLED'
                ].includes(order.status)
            ).length
          }
          note="Active fulfilment queue"
          tone="green"
          onClick={() => setTab('Prepare Orders')}
        />

        <Stat
          icon="⚠"
          label="Low / out of stock"
          value={lowStock.length}
          note="Review replenishment"
          tone="amber"
          onClick={() => setTab('Product Catalogue')}
        />

        <Stat
          icon="✓"
          label="Prescriptions loaded"
          value={prescriptions.length}
          note="Returned by the backend"
          tone="purple"
          onClick={() => setTab('Prescription Queue')}
        />
      </div>

      <div className="ph-overview-grid">
        <div className="ph-panel">
          <div className="ph-panel-heading">
            <div>
              <h2>Prescription queue</h2>
              <p>Recent prescriptions returned by the backend</p>
            </div>

            <button
              className="ph-link"
              onClick={() => setTab('Prescription Queue')}
            >
              View queue →
            </button>
          </div>

          <div className="ph-queue-list">
            {prescriptionsLoading && prescriptions.length === 0 ? (
              <div className="ph-empty">Loading prescriptions…</div>
            ) : recentPrescriptions.length === 0 ? (
              <div className="ph-empty">
                No pending prescriptions were returned.
              </div>
            ) : (
              recentPrescriptions.map(prescription => (
                <div
                  className="ph-queue-row"
                  key={prescription.prescriptionId}
                >
                  <span className="ph-queue-icon">▧</span>

                  <div className="ph-queue-copy">
                    <strong>
                      {prescription.prescriptionId} · Patient{' '}
                      {prescription.patientId || '—'}
                    </strong>
                    <span>
                      Product {prescription.productId || '—'} · Issued{' '}
                      {formatDate(prescription.issuedDate)}
                    </span>
                  </div>

                  {badge(
                    getPrescriptionStatus(
                      prescription.verificationStatus
                    )
                  )}

                  <button
                    className="ph-arrow"
                    aria-label={`Open ${prescription.prescriptionId}`}
                    onClick={() => setTab('Prescription Queue')}
                  >
                    →
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="ph-panel">
          <div className="ph-panel-heading">
            <div>
              <h2>Stock alerts</h2>
              <p>Items that may need replenishment</p>
            </div>

            <button
              className="ph-link"
              onClick={() => setTab('Product Catalogue')}
            >
              Inventory →
            </button>
          </div>

          <div className="ph-stock-list">
            {lowStock.slice(0, 4).map(product => (
              <div key={product.id}>
                <span className="ph-stock-emoji"><img src={`/images/products/${String(product.id || '').toUpperCase()}.webp`} alt={product.name} onError={event => { event.currentTarget.style.display = 'none'; }} /></span>

                <div>
                  <strong>{product.name}</strong>
                  <small>{product.category}</small>
                </div>

                <span
                  className={`ph-stock-count ${
                    product.stock === 0 ? 'out' : ''
                  }`}
                >
                  {product.stock === 0
                    ? 'Out'
                    : `${product.stock} left`}
                </span>
              </div>
            ))}

            {lowStock.length === 0 && (
              <div className="ph-empty">No low-stock products.</div>
            )}
          </div>
        </div>
      </div>

      <div className="ph-panel ph-tasks">
        <div className="ph-panel-heading">
          <div>
            <h2>Order preparation</h2>
            <p>Latest orders from the backend</p>
          </div>

          <button
            className="ph-link"
            onClick={() => setTab('Prepare Orders')}
          >
            Manage orders →
          </button>
        </div>

        <div className="ph-table-wrap">
          <table>
            <thead>
              <tr>
                <th>Order reference</th>
                <th>Customer</th>
                <th>Items</th>
                <th>Current status</th>
                <th></th>
              </tr>
            </thead>

            <tbody>
              {orders.slice(0, 3).map(order => (
                <tr key={order.id}>
                  <td>
                    <strong>{order.id}</strong>
                  </td>
                  <td>{order.customer}</td>
                  <td>{order.items} items</td>
                  <td>
                    {badge(String(order.status).replaceAll('_', ' '))}
                  </td>
                  <td>
                    <button
                      className="ph-btn ph-btn-small"
                      onClick={() => setTab('Prepare Orders')}
                    >
                      Open
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {orders.length === 0 && (
            <div className="ph-empty">No orders available.</div>
          )}
        </div>
      </div>

      <p className="ph-disclaimer">
        Prescription and order information is loaded from the backend.
        Patient records require a separate secure API integration.
      </p>

    </section>
  );
}

function Stat({ icon, label, value, note, tone, onClick }) {
  return (
    <button className="ph-stat-card" onClick={onClick}>
      <span className={`ph-stat-icon ${tone}`}>{icon}</span>
      <span className="ph-stat-label">{label}</span>
      <strong>{value}</strong>
      <small>{note}</small>
      <span className="ph-stat-open">Open section ↗</span>
    </button>
  );
}