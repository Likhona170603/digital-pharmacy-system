import React, { useCallback, useEffect, useMemo, useState } from 'react';
import '../styles/courier.css';
import api, { getStoredUser } from '../services/api';

const statusOptions = [
  { value: 'PENDING', label: 'Pending assignment' },
  { value: 'PACKED', label: 'Ready for pickup' },
  { value: 'DISPATCHED', label: 'Dispatched' },
  { value: 'IN_TRANSIT', label: 'In transit' },
  { value: 'DELIVERED', label: 'Delivered' },
  { value: 'CANCELLED', label: 'Cancelled' }
];
const label = value => statusOptions.find(option => option.value === value)?.label || String(value || 'Unknown').replaceAll('_', ' ');
const statusClass = status => status === 'DELIVERED' ? 'green' : status === 'CANCELLED' ? 'red' : status === 'PACKED' || status === 'PENDING' ? 'amber' : 'blue';
const asDelivery = item => ({
  ...item,
  id: item.id || item.deliveryId || item.trackingNo || '—',
  order: item.order?.orderNumber || item.orderNumber || '—',
  customer: item.order?.customerId || item.customerId || '—',
  address: item.address || 'Address not supplied',
  status: item.status || 'PENDING',
  items: Array.isArray(item.order?.items) ? item.order.items.map(line => `${line.product?.productName || 'Item'} × ${line.orderQty || 1}`).join(', ') : 'Order details unavailable',
  time: item.dispatchedAt ? new Date(item.dispatchedAt).toLocaleString() : 'Not dispatched',
  recipientName: item.recipientName || ''
});

export default function CourierDashboard({ onExit }) {
  const user = getStoredUser();
  const [deliveries, setDeliveries] = useState([]);
  const [filter, setFilter] = useState('All');
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState(null);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [recipientName, setRecipientName] = useState('');

  const load = useCallback(async () => {
    if (!user?.id) {
      setLoading(false);
      setError('Your courier account could not be identified. Sign out and sign in again.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const result = await api.get(`/delivery/courier/${encodeURIComponent(user.id)}/manifest`);
      setDeliveries((Array.isArray(result) ? result : []).map(asDelivery));
    } catch (err) {
      setError(err.message || 'Could not load your assigned deliveries.');
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => { load(); }, [load]);

  const visible = useMemo(() => deliveries.filter(d =>
    (filter === 'All' || d.status === filter) &&
    `${d.id} ${d.order} ${d.customer} ${d.address} ${d.status}`.toLowerCase().includes(query.toLowerCase())
  ), [deliveries, filter, query]);

  const updateStatus = async (delivery, status) => {
    if (!delivery?.id) return;
    if (status === 'DELIVERED' && !recipientName.trim() && !delivery.recipientName) {
      setError('Enter the recipient name before confirming delivery.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const params = new URLSearchParams({ status });
      if (status === 'DELIVERED') params.set('recipientName', (recipientName || delivery.recipientName).trim());
      const updated = await api.patch(`/delivery/${encodeURIComponent(delivery.id)}/status?${params.toString()}`);
      const mapped = asDelivery(updated || { ...delivery, status });
      setDeliveries(current => current.map(item => item.id === delivery.id ? mapped : item));
      setSelected(null);
      setRecipientName('');
      setNotice(`Delivery ${delivery.id} updated to ${label(status)}.`);
      window.setTimeout(() => setNotice(''), 3500);
    } catch (err) {
      setError(err.message || 'Could not update the delivery status.');
    } finally {
      setBusy(false);
    }
  };

  const openSelected = delivery => {
    setSelected(delivery);
    setRecipientName(delivery.recipientName || '');
    setError('');
  };

  const pending = deliveries.filter(d => !['DELIVERED', 'CANCELLED'].includes(d.status)).length;
  const delivered = deliveries.filter(d => d.status === 'DELIVERED').length;
  const issues = deliveries.filter(d => d.status === 'CANCELLED').length;
  const transit = deliveries.filter(d => ['DISPATCHED', 'IN_TRANSIT'].includes(d.status)).length;
  const percent = Math.round(delivered / Math.max(deliveries.length, 1) * 100);

  return <div className="courier">
    <header className="cr-topbar">
      <button className="cr-brand" onClick={onExit} aria-label="Return to public website"><span className="cr-mark">✚</span><span>PharmaFlow<small>Courier workspace</small></span></button>
      <div className="cr-top-actions"><span className="cr-online"><i /> Signed in as {user?.fullName || 'Courier'}</span><button className="cr-btn cr-outline" onClick={onExit}>Exit workspace</button></div>
    </header>

    <div className="cr-layout">
      <aside className="cr-sidebar">
        <div className="cr-side-label">WORKSPACE</div>
        <button className="cr-nav active"><span>▦</span> Overview</button>
        <button className="cr-nav" onClick={() => {setFilter('All'); document.getElementById('cr-deliveries')?.scrollIntoView({behavior:'smooth'});}}><span>↗</span> My deliveries <b>{pending}</b></button>
        <button className="cr-nav" onClick={() => {setFilter('DELIVERED'); document.getElementById('cr-deliveries')?.scrollIntoView({behavior:'smooth'});}}><span>✓</span> Delivery history</button>
        <div className="cr-help"><strong>Delivery support</strong><p>Contact the pharmacy team if an assigned delivery is missing, damaged or cannot be completed.</p><button onClick={() => setNotice('Please use your pharmacy’s configured support channel for delivery assistance.')}>Support guidance</button></div>
      </aside>

      <main className="cr-main">
        <div className="cr-page-head"><div><div className="cr-eyebrow">{new Date().toLocaleDateString(undefined,{weekday:'long',month:'long',day:'numeric'})} · COURIER OPERATIONS</div><h1>Good day, {user?.fullName || 'Courier'} 👋</h1><p>Here is your assigned delivery schedule and progress.</p></div><button className="cr-btn cr-primary" onClick={() => {setFilter('All'); document.getElementById('cr-deliveries')?.scrollIntoView({behavior:'smooth'});}}>View delivery list ↓</button></div>

        {notice && <div className="cr-notice" role="status">{notice}</div>}
        {error && <div className="cr-notice" role="alert">{error} <button onClick={load} disabled={loading}>Retry</button></div>}

        <section className="cr-stats">
          <article className="cr-stat"><div className="cr-stat-icon blue">↗</div><span>Assigned deliveries</span><strong>{deliveries.length}</strong><small>In your manifest</small></article>
          <article className="cr-stat"><div className="cr-stat-icon amber">◷</div><span>In progress</span><strong>{transit}</strong><small>Dispatched or on the way</small></article>
          <article className="cr-stat"><div className="cr-stat-icon green">✓</div><span>Delivered</span><strong>{delivered}</strong><small>Confirmed deliveries</small></article>
          <article className="cr-stat"><div className="cr-stat-icon red">!</div><span>Cancelled</span><strong>{issues}</strong><small>Not completed</small></article>
        </section>

        <section className="cr-progress-grid">
          <article className="cr-panel cr-route-panel"><div className="cr-panel-head"><div><h2>Delivery progress</h2><p>Current manifest completion</p></div><span className="cr-pill blue">Live manifest</span></div><div className="cr-progress-line"><span style={{width:`${percent}%`}} /></div><div className="cr-progress-foot"><strong>{delivered} of {deliveries.length} delivered</strong><span>{percent}% complete</span></div><div className="cr-route-note"><span className="cr-route-icon">🗺️</span><div><strong>Delivery route</strong><p>Assigned delivery addresses are shown below. Route optimisation is not provided by the current backend.</p></div></div></article>
          <article className="cr-panel"><div className="cr-panel-head"><div><h2>Delivery checklist</h2><p>Before marking an order complete</p></div></div><ul className="cr-checklist"><li><span>✓</span> Confirm the order reference</li><li><span>✓</span> Verify the delivery address</li><li><span>✓</span> Follow medicine handling instructions</li><li><span>✓</span> Record the recipient name</li></ul></article>
        </section>

        <section className="cr-panel cr-deliveries-panel" id="cr-deliveries">
          <div className="cr-panel-head cr-list-head"><div><h2>My deliveries</h2><p>Review assigned orders and save status changes to the server.</p></div><span className="cr-pill">{visible.length} records</span></div>
          <div className="cr-toolbar"><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search delivery, order, customer or area…" aria-label="Search deliveries"/><select value={filter} onChange={e=>setFilter(e.target.value)} aria-label="Filter by status"><option value="All">All statuses</option>{statusOptions.map(s=><option key={s.value} value={s.value}>{s.label}</option>)}</select><button className="cr-btn cr-outline" onClick={load} disabled={loading}>{loading?'Loading…':'Refresh'}</button></div>
          <div className="cr-table-wrap"><table className="cr-table"><thead><tr><th>Delivery / Order</th><th>Customer & destination</th><th>Dispatched</th><th>Status</th><th>Action</th></tr></thead><tbody>{visible.map(d=><tr key={d.id}><td><strong>{d.id}</strong><small>{d.order} · {d.items}</small></td><td><strong>Customer #{d.customer}</strong><small>{d.address}</small></td><td>{d.time}</td><td><span className={`cr-pill ${statusClass(d.status)}`}>{label(d.status)}</span></td><td><button className="cr-btn cr-small" onClick={()=>openSelected(d)}>View / update</button></td></tr>)}</tbody></table>{visible.length===0&&<div className="cr-empty">{loading?'Loading assigned deliveries…':'No deliveries match those filters.'}</div>}</div>
          <div className="cr-disclaimer">Only deliveries assigned to the signed-in courier are shown.</div>
        </section>
      </main>
    </div>

    {selected && <div className="cr-modal-backdrop" role="presentation" onMouseDown={e=>{if(e.target===e.currentTarget&&!busy)setSelected(null)}}><section className="cr-modal" role="dialog" aria-modal="true" aria-labelledby="cr-modal-title"><button className="cr-modal-close" onClick={()=>setSelected(null)} aria-label="Close" disabled={busy}>×</button><div className="cr-eyebrow">DELIVERY DETAILS</div><h2 id="cr-modal-title">{selected.id} <span className={`cr-pill ${statusClass(selected.status)}`}>{label(selected.status)}</span></h2><div className="cr-detail-grid"><div><label>Order reference</label><strong>{selected.order}</strong></div><div><label>Customer ID</label><strong>{selected.customer}</strong></div><div><label>Destination</label><strong>{selected.address}</strong></div><div><label>Recipient name</label><strong>{selected.recipientName || 'Not recorded'}</strong></div><div><label>Order contents</label><strong>{selected.items}</strong></div><div><label>Dispatch time</label><strong>{selected.time}</strong></div></div><div className="cr-field"><label htmlFor="cr-status">Update delivery status</label><select id="cr-status" value={selected.status} onChange={e=>setSelected({...selected,status:e.target.value})} disabled={busy}>{statusOptions.map(s=><option key={s.value} value={s.value}>{s.label}</option>)}</select></div><div className="cr-field"><label htmlFor="cr-recipient">Recipient name (required for delivery confirmation)</label><input id="cr-recipient" value={recipientName} onChange={e=>setRecipientName(e.target.value)} disabled={busy} /></div><div className="cr-modal-actions"><button className="cr-btn cr-outline" onClick={()=>setSelected(null)} disabled={busy}>Cancel</button><button className="cr-btn cr-primary" disabled={busy||selected.status===deliveries.find(d=>d.id===selected.id)?.status} onClick={()=>updateStatus(selected,selected.status)}>{busy?'Saving…':'Save status'}</button></div><p className="cr-sensitive-note">Only confirm delivery after following the pharmacy's proof-of-delivery procedure.</p></section></div>}
  </div>;
}
