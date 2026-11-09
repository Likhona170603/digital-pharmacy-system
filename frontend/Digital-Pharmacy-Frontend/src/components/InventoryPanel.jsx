import React, { useMemo, useState } from 'react';
import api from '../services/api';
import { LOW_STOCK_THRESHOLD } from '../hooks/useInventory';

const emptyForm = {
  productId: '',
  productName: '',
  sku: '',
  description: '',
  price: '0',
  stockQuantity: '0',
  categoryId: '',
  rxRequired: false
};

function describeError(err, fallback) {
  if (err instanceof TypeError) {
    return 'Cannot connect to the server. Make sure the backend is running.';
  }
  if (err.status === 409 && /status 409/.test(err.message)) {
    return 'A product with that ID or SKU already exists.';
  }
  if (err.status === 400 && /status 400/.test(err.message)) {
    return 'Check the details: name, SKU and category are required, and price and stock cannot be negative.';
  }
  return err.message || fallback;
}

function nextProductId(inventory) {
  let max = 0;
  inventory.forEach(p => {
    const match = /^P(\d+)$/i.exec(p.id);
    if (match) max = Math.max(max, Number(match[1]));
  });
  return `P${String(max + 1).padStart(3, '0')}`;
}

export default function InventoryPanel({
  inventory,
  categories,
  loading,
  error,
  onReload,
  notify,
  log,
  money
}) {
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('All');
  const [modal, setModal] = useState(null); // { mode: 'create' | 'edit' | 'stock', item? }
  const [form, setForm] = useState(emptyForm);
  const [stockValue, setStockValue] = useState('0');
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();

    return inventory.filter(p => {
      const matchesQuery =
        !q || `${p.id} ${p.name} ${p.sku} ${p.category}`.toLowerCase().includes(q);

      const matchesFilter =
        filter === 'All' ||
        (filter === 'Low' && p.active && p.stock <= p.reorder) ||
        (filter === 'Inactive' && !p.active);

      return matchesQuery && matchesFilter;
    });
  }, [inventory, query, filter]);

  const notStaff = error && (error.status === 401 || error.status === 403);

  function closeModal() {
    setModal(null);
    setFormError('');
    setSaving(false);
  }

  function openCreate() {
    setForm({
      ...emptyForm,
      productId: nextProductId(inventory),
      categoryId: categories[0]?.categoryId || ''
    });
    setFormError('');
    setModal({ mode: 'create' });
  }

  function openEdit(item) {
    setForm({
      productId: item.id,
      productName: item.name,
      sku: item.sku,
      description: item.description,
      price: String(item.price),
      stockQuantity: String(item.stock),
      categoryId: item.categoryId,
      rxRequired: item.rx
    });
    setFormError('');
    setModal({ mode: 'edit', item });
  }

  function openStock(item) {
    setStockValue(String(item.stock));
    setFormError('');
    setModal({ mode: 'stock', item });
  }

  function setField(name, value) {
    setForm(current => ({ ...current, [name]: value }));
  }

  function validateForm() {
    if (!form.productId.trim()) return 'Enter a product ID.';
    if (!form.productName.trim()) return 'Enter a product name.';
    if (!form.sku.trim()) return 'Enter a SKU.';
    if (!form.categoryId) return 'Choose a category.';
    if (form.price === '' || Number(form.price) < 0) return 'Price cannot be negative.';

    if (modal.mode === 'create') {
      const stock = Number(form.stockQuantity);
      if (form.stockQuantity === '' || !Number.isInteger(stock) || stock < 0) {
        return 'Stock must be a whole number of 0 or more.';
      }
    }

    return '';
  }

  async function saveProduct(e) {
    e.preventDefault();

    const problem = validateForm();
    if (problem) {
      setFormError(problem);
      return;
    }

    setSaving(true);
    setFormError('');

    try {
      const body = {
        productId: form.productId.trim(),
        productName: form.productName.trim(),
        description: form.description.trim(),
        price: Number(form.price),
        sku: form.sku.trim(),
        rxRequired: form.rxRequired,
        category: { categoryId: form.categoryId }
      };

      if (modal.mode === 'create') {
        await api.post('/product/create', {
          ...body,
          stockQuantity: Number(form.stockQuantity)
        });
        log?.('Added product', body.productName);
        notify(`${body.productName} was added.`);
      } else {
        // Re-read the stock so an edit can never overwrite sales made in the meantime.
        const fresh = await api.get(`/product/read/${encodeURIComponent(body.productId)}`);

        await api.post('/product/update', {
          ...body,
          stockQuantity: fresh.stockQuantity
        });
        log?.('Edited product', body.productName);
        notify(`${body.productName} was updated.`);
      }

      closeModal();
      await onReload();
    } catch (err) {
      setFormError(describeError(err, 'Could not save the product.'));
      setSaving(false);
    }
  }

  async function saveStock(e) {
    e.preventDefault();

    const quantity = Number(stockValue);
    if (stockValue === '' || !Number.isInteger(quantity) || quantity < 0) {
      setFormError('Stock must be a whole number of 0 or more.');
      return;
    }

    setSaving(true);
    setFormError('');

    try {
      await api.patch(`/product/${encodeURIComponent(modal.item.id)}/stock`, {
        quantity
      });
      log?.('Set stock level', `${modal.item.name}: ${quantity}`);
      notify(`Stock for ${modal.item.name} is now ${quantity}.`);
      closeModal();
      await onReload();
    } catch (err) {
      setFormError(describeError(err, 'Could not update the stock.'));
      setSaving(false);
    }
  }

  async function toggleActive(item) {
    const next = !item.active;

    try {
      await api.patch(`/product/${encodeURIComponent(item.id)}/active?value=${next}`);
      log?.(next ? 'Activated product' : 'Deactivated product', item.name);
      notify(
        next
          ? `${item.name} is visible in the catalogue again.`
          : `${item.name} is hidden from the catalogue.`
      );
      await onReload();
    } catch (err) {
      notify(describeError(err, 'Could not change the product status.'));
    }
  }

  function statusBadge(p) {
    if (!p.active) return <span className="ad-badge blue">Inactive</span>;
    if (p.stock <= 0) return <span className="ad-badge red">Out of stock</span>;
    if (p.stock <= p.reorder) return <span className="ad-badge amber">Low stock</span>;
    return <span className="ad-badge green">In stock</span>;
  }

  return (
    <section className="ad-panel">
      <div className="ad-panel-head">
        <div>
          <h2>Inventory management</h2>
          <p>Products, prices and stock levels saved in the database.</p>
        </div>
        <button
          className="ad-btn ad-primary"
          onClick={openCreate}
          disabled={Boolean(error) || categories.length === 0}
        >
          ＋ Add product
        </button>
      </div>

      {notStaff && (
        <div className="ad-error" role="alert">
          Inventory is only available to staff. Log in with a pharmacist or admin
          account, then open this screen again.
        </div>
      )}

      {error && !notStaff && (
        <div className="ad-error" role="alert">
          {describeError(error, 'Could not load the inventory.')}
        </div>
      )}

      {!error && !loading && categories.length === 0 && (
        <div className="ad-error" role="alert">
          There are no categories yet. Add a category before adding products.
        </div>
      )}

      <div className="ad-toolbar">
        <input
          placeholder="Search name, ID, SKU or category…"
          value={query}
          onChange={e => setQuery(e.target.value)}
        />
        <select value={filter} onChange={e => setFilter(e.target.value)}>
          <option value="All">All products</option>
          <option value="Low">Low stock ({LOW_STOCK_THRESHOLD} or fewer)</option>
          <option value="Inactive">Inactive only</option>
        </select>
        <button className="ad-btn ad-ghost" onClick={onReload} disabled={loading}>
          {loading ? 'Loading…' : 'Refresh'}
        </button>
      </div>

      <div className="ad-table-wrap">
        <table className="ad-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock on hand</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rows.map(p => (
              <tr key={p.id}>
                <td>
                  <strong>{p.name}</strong>
                  <small>
                    {p.id} · {p.sku}
                    {p.rx ? ' · Rx' : ''}
                  </small>
                </td>
                <td>{p.category}</td>
                <td>{money(p.price)}</td>
                <td><strong>{p.stock}</strong></td>
                <td>{statusBadge(p)}</td>
                <td>
                  <div className="ad-row-actions">
                    <button className="ad-btn ad-ghost" onClick={() => openEdit(p)}>Edit</button>
                    <button className="ad-btn ad-ghost" onClick={() => openStock(p)}>Stock</button>
                    <button className="ad-btn ad-ghost" onClick={() => toggleActive(p)}>
                      {p.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {!loading && !error && rows.length === 0 && (
          <div className="ad-empty">No products match your search.</div>
        )}
      </div>

      <p className="ad-footnote">
        Deactivating hides a product from the catalogue and blocks new orders for it.
        Products are deactivated rather than deleted so past orders keep their history.
      </p>

      {modal && modal.mode !== 'stock' && (
        <div
          className="ad-modal-backdrop"
          onMouseDown={e => {
            if (e.target === e.currentTarget) closeModal();
          }}
        >
          <form className="ad-modal" onSubmit={saveProduct}>
            <button type="button" className="ad-close" onClick={closeModal}>×</button>
            <div className="ad-eyebrow">INVENTORY</div>
            <h2>{modal.mode === 'create' ? 'Add product' : 'Edit product'}</h2>

            <div className="ad-form-grid ad-two">
              <label>
                Product ID
                <input
                  value={form.productId}
                  onChange={e => setField('productId', e.target.value)}
                  readOnly={modal.mode === 'edit'}
                />
              </label>
              <label>
                SKU
                <input value={form.sku} onChange={e => setField('sku', e.target.value)} />
              </label>
            </div>

            <label>
              Product name
              <input
                value={form.productName}
                onChange={e => setField('productName', e.target.value)}
              />
            </label>

            <label>
              Description
              <textarea
                rows="3"
                value={form.description}
                onChange={e => setField('description', e.target.value)}
              />
            </label>

            <label>
              Category
              <select
                value={form.categoryId}
                onChange={e => setField('categoryId', e.target.value)}
              >
                {categories.map(c => (
                  <option key={c.categoryId} value={c.categoryId}>
                    {c.categoryName}
                  </option>
                ))}
              </select>
            </label>

            <div className="ad-form-grid ad-two">
              <label>
                Price (R)
                <input
                  type="number"
                  min="0"
                  step=".01"
                  value={form.price}
                  onChange={e => setField('price', e.target.value)}
                />
              </label>
              {modal.mode === 'create' && (
                <label>
                  Opening stock
                  <input
                    type="number"
                    min="0"
                    step="1"
                    value={form.stockQuantity}
                    onChange={e => setField('stockQuantity', e.target.value)}
                  />
                </label>
              )}
            </div>

            <label className="ad-check">
              <input
                type="checkbox"
                checked={form.rxRequired}
                onChange={e => setField('rxRequired', e.target.checked)}
              />
              Prescription required (Rx)
            </label>

            {formError && <div className="ad-error" role="alert">{formError}</div>}

            <div className="ad-modal-actions">
              <button type="button" className="ad-btn ad-ghost" onClick={closeModal}>
                Cancel
              </button>
              <button className="ad-btn ad-primary" disabled={saving}>
                {saving ? 'Saving…' : modal.mode === 'create' ? 'Add product' : 'Save changes'}
              </button>
            </div>
          </form>
        </div>
      )}

      {modal && modal.mode === 'stock' && (
        <div
          className="ad-modal-backdrop"
          onMouseDown={e => {
            if (e.target === e.currentTarget) closeModal();
          }}
        >
          <form className="ad-modal" onSubmit={saveStock}>
            <button type="button" className="ad-close" onClick={closeModal}>×</button>
            <div className="ad-eyebrow">INVENTORY</div>
            <h2>Set stock level</h2>
            <p className="ad-muted">
              {modal.item.name} currently has {modal.item.stock} in stock.
            </p>

            <label>
              New stock quantity
              <input
                type="number"
                min="0"
                step="1"
                value={stockValue}
                onChange={e => setStockValue(e.target.value)}
                autoFocus
              />
            </label>

            {formError && <div className="ad-error" role="alert">{formError}</div>}

            <div className="ad-modal-actions">
              <button type="button" className="ad-btn ad-ghost" onClick={closeModal}>
                Cancel
              </button>
              <button className="ad-btn ad-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Save stock'}
              </button>
            </div>
          </form>
        </div>
      )}
    </section>
  );
}
