import React, { useEffect, useMemo, useState } from 'react';
import { categories } from '../data/categories';
import { stockStatus, money } from '../data/products';
import '../styles/products.css';
import { useCart } from '../context/CartContext';
import { photoFor } from '../utils/productImages';
import api from '../services/api';


/* ---------- Product photos ---------- */


function ProductPhoto({ product }) {
  const [failed, setFailed] = useState(false);
  return (
    <span className="pr-media">
      {failed ? (
        <span className="pr-illus" style={{ fontSize: 56 }}>{product.icon}</span>
      ) : (
        <img
          className="pr-photo"
          src={product.image || photoFor(product)}
          alt={product.name}
          loading="lazy"
          onError={() => setFailed(true)}
        />
      )}
    </span>
  );
}

/* ---------- Data normalising ---------- */

function normalizeProduct(p) {
  const categoryValue = p.category;
  const categoryName =
    typeof categoryValue === 'string'
      ? categoryValue
      : categoryValue?.categoryName ??
        categoryValue?.name ??
        p.categoryName ??
        'Other';

  const rxValue =
    p.rxRequired ??
    p.isRxRequired ??
    p.is_rx_required ??
    p.rx ??
    p.prescriptionRequired ??
    p.requiresPrescription ??
    false;

  const rx =
    typeof rxValue === 'string'
      ? ['true', 'yes', 'rx', '1'].includes(rxValue.toLowerCase())
      : Boolean(rxValue);

  const categoryInfo = categories.find(
    c => c.name.toLowerCase() === categoryName.toLowerCase()
  );

  return {
    id: String(p.productId ?? p.product_id ?? p.id ?? p.sku),
    name: p.productName ?? p.product_name ?? p.name ?? 'Unnamed product',
    category: categoryName,
    price: Number(p.price ?? p.unitPrice ?? p.productPrice ?? 0),
    stock: Number(
      p.stockQuantity ?? p.stock_quantity ?? p.stock ?? p.quantityInStock ?? 0
    ),
    desc:
      p.description ??
      p.productDescription ??
      p.desc ??
      'No description available.',
    icon: categoryInfo?.icon ?? p.icon ?? '💊',
    rx,
    form:
      p.category?.dosageForm ??
      p.dosageForm ??
      p.form ??
      'Not specified',
    schedule:
      p.category?.scheduleClassification ??
      p.scheduleClassification ??
      p.schedule ??
      (rx ? 'Prescription required' : 'Over the counter'),
    image: p.imageUrl ?? p.image_url ?? p.image ?? p.imagePath ?? p.imageName ?? '',
    active: p.active ?? true
  };
}

/* ---------- Page ---------- */

export default function Products({
  initialSearch = '',
  initialCategory = 'All',
  onNotify
}) {
  const { addToCart, cart } = useCart();

  const [products, setProducts] = useState([]);
  const [search, setSearch] = useState(initialSearch);
  const [category, setCategory] = useState(initialCategory);
  const [type, setType] = useState('all');
  const [inStockOnly, setInStockOnly] = useState(false);
  const [sort, setSort] = useState('featured');
  const [selected, setSelected] = useState(null);
  const [qty, setQty] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadProducts() {
    setLoading(true);
    setError('');

    try {
      const data = await api.get('/product/catalogue?size=200');

      const records = Array.isArray(data)
        ? data
        : data.content ?? data.products ?? data.data ?? data.items ?? [];

      if (!Array.isArray(records)) {
        throw new Error('The catalogue response has an unexpected format.');
      }

      setProducts(records.map(normalizeProduct).filter(p => p.active));
    } catch (err) {
      setError(err.message || 'Could not load the product catalogue. Please try again.');
      console.error('Product catalogue error:', err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProducts();
  }, []);

  useEffect(() => {
    setSearch(initialSearch);
    setCategory(initialCategory);
  }, [initialSearch, initialCategory]);

  useEffect(() => {
    if (!selected) return;

    const onKey = e => {
      if (e.key === 'Escape') setSelected(null);
    };

    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [selected]);

  const list = useMemo(() => {
    const q = search.trim().toLowerCase();

    let out = products.filter(
      p =>
        (category === 'All' || p.category === category) &&
        (type === 'all' || (type === 'rx') === p.rx) &&
        (!inStockOnly || p.stock > 0) &&
        (!q ||
          `${p.name} ${p.category} ${p.desc}`
            .toLowerCase()
            .includes(q))
    );

    if (sort === 'price-asc') {
      out = [...out].sort((a, b) => a.price - b.price);
    }

    if (sort === 'price-desc') {
      out = [...out].sort((a, b) => b.price - a.price);
    }

    if (sort === 'name') {
      out = [...out].sort((a, b) => a.name.localeCompare(b.name));
    }

    return out;
  }, [products, search, category, type, inStockOnly, sort]);

  const open = p => {
    setSelected(p);
    setQty(1);
  };

  const add = (product, quantity = 1) => {
    let savedUser = null;
    try {
      const raw = localStorage.getItem('pharmaflow_user') || sessionStorage.getItem('pharmaflow_user');
      savedUser = raw ? JSON.parse(raw) : null;
    } catch {
      savedUser = null;
    }
    const role = String(savedUser?.role || savedUser?.userRole || '').trim().toUpperCase();
    if (!savedUser || !['CUSTOMER'].includes(role)) {
      onNotify?.(!savedUser ? 'Please sign in with a customer account before adding products to your cart.' : 'Only customer accounts can shop.');
      window.dispatchEvent(new CustomEvent('pharmaflow-require-customer-login'));
      return;
    }

    const existing = cart.find(item => item.id === product.id);
    const currentQuantity = existing?.quantity ?? 0;
    const available = product.stock - currentQuantity;

    if (available <= 0) {
      onNotify?.(`No more stock is available for ${product.name}.`);
      return;
    }

    const quantityToAdd = Math.min(quantity, available);

    addToCart({ ...product, image: product.image || photoFor(product) }, quantityToAdd);

    onNotify?.(
      `${quantityToAdd} × ${product.name} added to your cart.`
    );

    setSelected(null);
  };

  const reset = () => {
    setSearch('');
    setCategory('All');
    setType('all');
    setInStockOnly(false);
  };

  const count = name =>
    products.filter(p => name === 'All' || p.category === name).length;

  const categoryNames = [
    'All',
    ...new Set([
      ...categories.map(c => c.name),
      ...products.map(p => p.category)
    ])
  ];

  return (
    <div className="pr">
      <section className="pr-intro">
        <div>
          <span className="pr-eyebrow">Catalogue</span>
          <h1>Products</h1>
          <p>
            Browse everyday medicine, vitamins and health essentials. Products
            marked Rx need a pharmacist-approved prescription before they can
            be dispensed.
          </p>
        </div>
        <p className="pr-cartline">
          <strong>Cart items: {cart.reduce((sum, item) => sum + item.quantity, 0)}</strong>
        </p>
      </section>

      <section className="pr-bar" aria-label="Filters">
        <div className="pr-bar-row">
          <div className="pr-search">
            <label className="pr-sr" htmlFor="pr-search">
              Search
            </label>
            <input
              id="pr-search"
              className="pr-input"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search by name or symptom"
            />
          </div>

          <fieldset className="pr-seg">
            <legend className="pr-sr">Type</legend>
            {[
              ['all', 'All medicines'],
              ['otc', 'Over the counter'],
              ['rx', 'Prescription (Rx)']
            ].map(([value, label]) => (
              <label className="pr-seg-opt" key={value}>
                <input
                  type="radio"
                  name="type"
                  checked={type === value}
                  onChange={() => setType(value)}
                />
                <span>{label}</span>
              </label>
            ))}
          </fieldset>

          <label className="pr-check">
            <input
              type="checkbox"
              checked={inStockOnly}
              onChange={e => setInStockOnly(e.target.checked)}
            />
            In stock only
          </label>
        </div>

        <h2 className="pr-sr">Category</h2>
        <ul className="pr-cats">
          {categoryNames.map(name => (
            <li key={name}>
              <button
                className={category === name ? 'on' : ''}
                onClick={() => setCategory(name)}
              >
                <span>{name === 'All' ? 'All products' : name}</span>
                <small>{count(name)}</small>
              </button>
            </li>
          ))}
        </ul>
      </section>

      <section className="pr-results" aria-live="polite">
        <div className="pr-toolbar">
          <p>
            <strong>{loading ? '…' : list.length}</strong>{' '}
            {list.length === 1 ? 'product' : 'products'}
            {category !== 'All' && <> in {category}</>}
          </p>

          <div className="pr-toolbar-right">
            <button className="pr-reset" onClick={reset}>
              Clear all filters
            </button>

            <label>
              Sort by
              <select value={sort} onChange={e => setSort(e.target.value)}>
                <option value="featured">Featured</option>
                <option value="price-asc">Price: low to high</option>
                <option value="price-desc">Price: high to low</option>
                <option value="name">Name: A to Z</option>
              </select>
            </label>
          </div>
        </div>

        {loading ? (
          <div className="pr-empty">
            <h2>Loading products…</h2>
            <p>Getting the latest catalogue from the server.</p>
          </div>
        ) : error ? (
          <div className="pr-empty">
            <h2>Catalogue unavailable</h2>
            <p>{error}</p>
            <button className="hp-btn hp-btn-primary" onClick={loadProducts}>
              Retry
            </button>
          </div>
        ) : list.length === 0 ? (
          <div className="pr-empty">
            <h2>No products match your search</h2>
            <p>
              Try a different word, or clear the filters to see the full
              catalogue.
            </p>
            <button className="hp-btn hp-btn-primary" onClick={reset}>
              Clear filters
            </button>
          </div>
        ) : (
          <div className="pr-grid">
            {list.map(p => {
              const stock = stockStatus(p.stock);
              const cartItem = cart.find(item => item.id === p.id);
              const inCart = cartItem?.quantity ?? 0;

              return (
                <article className="pr-card" key={p.id}>
                  <button
                    className="pr-art"
                    onClick={() => open(p)}
                    aria-label={`View ${p.name}`}
                  >
                    <ProductPhoto product={p} />
                    {p.rx && <em className="pr-rx">Rx</em>}
                  </button>

                  <small>{p.category}</small>
                  <h3>{p.name}</h3>
                  <div className={`pr-stock ${stock.tone}`}>
                    {stock.label}
                  </div>

                  {inCart > 0 && <small className="pr-incart">In cart: {inCart}</small>}

                  <div className="pr-foot">
                    <strong>{money(p.price)}</strong>
                    <div>
                      <button
                        className="hp-btn hp-btn-small"
                        onClick={() => open(p)}
                      >
                        Details
                      </button>
                      <button
                        className="hp-btn hp-btn-small pr-add"
                        disabled={p.stock <= inCart}
                        onClick={() => add(p)}
                      >
                        {p.stock <= inCart ? 'Max in cart' : 'Add'}
                      </button>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>

      {selected && (
        <div
          className="pr-overlay"
          onClick={e => {
            if (e.target === e.currentTarget) setSelected(null);
          }}
        >
          <div
            className="pr-modal"
            role="dialog"
            aria-modal="true"
            aria-label={selected.name}
          >
            <button
              className="pr-close"
              onClick={() => setSelected(null)}
              aria-label="Close"
            >
              ×
            </button>

            <div className="pr-modal-art" aria-hidden="true">
              <ProductPhoto key={selected.id} product={selected} />
            </div>

            <div className="pr-modal-body">
              <small>{selected.category}</small>
              <h2>{selected.name}</h2>

              <div className="pr-modal-price">
                <strong>{money(selected.price)}</strong>
                <span className={`pr-stock ${stockStatus(selected.stock).tone}`}>
                  {stockStatus(selected.stock).label}
                </span>
              </div>

              <p>{selected.desc}</p>

              <dl>
                <div>
                  <dt>Form</dt>
                  <dd>{selected.form}</dd>
                </div>
                <div>
                  <dt>Schedule</dt>
                  <dd>{selected.schedule}</dd>
                </div>
                <div>
                  <dt>Type</dt>
                  <dd>
                    {selected.rx ? 'Prescription only' : 'Over the counter'}
                  </dd>
                </div>
              </dl>

              {selected.rx && (
                <div className="pr-note">
                  <b>Prescription required.</b> A pharmacist must verify the
                  prescription before the medicine can be dispensed.
                </div>
              )}

              <div className="pr-buy">
                <div className="pr-qty">
                  <button
                    onClick={() => setQty(q => Math.max(1, q - 1))}
                    aria-label="Decrease quantity"
                  >
                    −
                  </button>
                  <span>{qty}</span>
                  <button
                    onClick={() =>
                      setQty(q => Math.min(10, selected.stock, q + 1))
                    }
                    aria-label="Increase quantity"
                    disabled={qty >= 10 || qty >= selected.stock}
                  >
                    +
                  </button>
                </div>

                <button
                  className="hp-btn hp-btn-primary hp-btn-lg"
                  disabled={
                    selected.stock <= 0 ||
                    qty > selected.stock -
                      (cart.find(item => item.id === selected.id)?.quantity ?? 0)
                  }
                  onClick={() => add(selected, qty)}
                >
                  {selected.stock <= 0
                    ? 'Out of stock'
                    : `Add to cart · ${money(selected.price * qty)}`}
                </button>
              </div>

              <p className="pr-fine">
                Always read the label and follow the directions for use. Speak
                to a pharmacist if you are unsure.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}