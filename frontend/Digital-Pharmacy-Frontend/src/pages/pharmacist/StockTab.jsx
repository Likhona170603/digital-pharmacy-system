import { useState } from 'react';
import api from '../../services/api';
import { money } from '../../data/products';
import PageTitle from './PageTitle';

export default function StockTab({
  inventory,
  inventoryLoading,
  inventoryError,
  reloadInventory,
  query,
  setQuery,
  badge,
  notify
}) {
  const [stockItem, setStockItem] = useState(null);
  const [stockValue, setStockValue] = useState('');
  const [stockError, setStockError] = useState('');
  const [stockBusy, setStockBusy] = useState(false);

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

  return (
    <>
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
                    : inventoryError.message || 'Could not load products.'}
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

          <span className="ph-soft-label">{inventory.length} products</span>
        </div>

        <div className="ph-panel">
          <div className="ph-table-wrap">
            {inventoryLoading && inventory.length === 0 ? (
              <div className="ph-empty">Loading products from the backend…</div>
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
                            <img
                              className="ph-product-thumb"
                              src={`/images/products/${String(
                                product.id || ''
                              ).toUpperCase()}.webp`}
                              alt=""
                              onError={event => {
                                event.currentTarget.style.display = 'none';
                              }}
                            />
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
    </>
  );
}
