import React, { useCallback, useEffect, useMemo, useState } from 'react';
import '../styles/admin.css';
import InventoryPanel from '../components/InventoryPanel';
import useInventory from '../hooks/useInventory';
import api, { getStoredUser } from '../services/api';

const money = value => `R ${Number(value || 0).toFixed(2)}`;
const label = value => String(value || '—').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
const badge = value => /delivered|collected|active|paid/i.test(value) ? 'green' : /cancel|declined|suspend|issue|low/i.test(value) ? 'red' : /pending|processing|packed|ready|transit|shipped/i.test(value) ? 'amber' : 'blue';
const dateTime = value => value ? new Date(value).toLocaleString() : '—';

function normaliseUser(user) {
  return { ...user, active: user.active !== false, name: user.fullName || user.name || 'Unnamed user', role: label(user.role), status: user.active === false ? 'Suspended' : 'Active' };
}
function normaliseOrder(order) {
  return {
    ...order,
    id: order.orderNumber || order.id,
    customer: order.customerId || '—',
    total: Number(order.totalAmt ?? order.total ?? 0),
    status: order.orderStatus || order.status || 'PENDING',
    paymentStatus: order.paymentStatus || 'PENDING',
    type: order.orderType || '—'
  };
}

export default function AdminDashboard({ onExit }) {
  const [tab, setTab] = useState('Overview');
  const [users, setUsers] = useState([]);
  const [orders, setOrders] = useState([]);
  const [deliveries, setDeliveries] = useState([]);
  const [deliveryLoading, setDeliveryLoading] = useState(false);
  const [assigningDelivery, setAssigningDelivery] = useState('');
  const [courierChoices, setCourierChoices] = useState({});
  const [audit, setAudit] = useState([]);
  const [query, setQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('All roles');
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [busyOrder, setBusyOrder] = useState('');
  const [staffForm, setStaffForm] = useState({ fullName: '', email: '', phone: '', password: '', role: 'PHARMACIST' });
  const [creatingStaff, setCreatingStaff] = useState(false);
  const [staffSaving, setStaffSaving] = useState(false);
  const [busyUser, setBusyUser] = useState('');
  const { inventory, categories, loading: inventoryLoading, error: inventoryError, reload: reloadInventory } = useInventory();
  const currentUser = getStoredUser();

  const notify = text => { setNotice(text); window.setTimeout(() => setNotice(''), 3500); };
  const load = useCallback(async () => {
    setLoading(true);
    setDeliveryLoading(true);
    setError('');
    const results = await Promise.allSettled([
      api.get('/user/getAll'),
      api.get('/order/getall'),
      api.get('/auditlog/getall'),
      api.get('/delivery/getall')
    ]);
    const [userResult, orderResult, auditResult, deliveryResult] = results;
    const failures = [];
    if (userResult.status === 'fulfilled') setUsers((Array.isArray(userResult.value) ? userResult.value : []).map(normaliseUser));
    else failures.push(`users: ${userResult.reason?.message || 'failed'}`);
    if (orderResult.status === 'fulfilled') setOrders((Array.isArray(orderResult.value) ? orderResult.value : []).map(normaliseOrder));
    else failures.push(`orders: ${orderResult.reason?.message || 'failed'}`);
    if (auditResult.status === 'fulfilled') setAudit(Array.isArray(auditResult.value) ? auditResult.value : []);
    else failures.push(`audit log: ${auditResult.reason?.message || 'failed'}`);
    if (deliveryResult.status === 'fulfilled') setDeliveries(Array.isArray(deliveryResult.value) ? deliveryResult.value : []);
    else failures.push(`deliveries: ${deliveryResult.reason?.message || 'failed'}`);
    if (failures.length) setError(`Some administrator data could not be loaded (${failures.join('; ')}). Check permissions and retry.`);
    setLoading(false);
    setDeliveryLoading(false);
  }, []);

  useEffect(() => { load(); }, [load]);

  const filteredUsers = useMemo(() => users.filter(user =>
    (roleFilter === 'All roles' || user.role.toLowerCase() === roleFilter.toLowerCase()) &&
    `${user.id} ${user.name} ${user.email} ${user.role}`.toLowerCase().includes(query.toLowerCase())
  ), [users, roleFilter, query]);
  const activeInventory = inventory.filter(product => product.active !== false);
  const lowStock = activeInventory.filter(product => product.stock <= product.reorder).length;
  const deliveredOrders = orders.filter(order => ['DELIVERED', 'COLLECTED'].includes(order.status));
  const activeOrders = orders.filter(order => !['DELIVERED', 'COLLECTED', 'CANCELLED'].includes(order.status));

  const couriers = users.filter(user => String(user.role || '').toLowerCase() === 'courier');

  async function assignCourier(delivery) {
    const deliveryId = delivery.id ?? delivery.deliveryId;
    const courierId = courierChoices[String(deliveryId)];
    if (!deliveryId || !courierId) {
      setError('Choose a courier before assigning this delivery.');
      return;
    }
    setAssigningDelivery(String(deliveryId));
    setError('');
    try {
      await api.put(`/delivery/${encodeURIComponent(String(deliveryId))}/courier/${encodeURIComponent(String(courierId))}`, {});
      notify(`Delivery ${deliveryId} assigned to courier ${courierId}.`);
      await load();
    } catch (err) {
      setError(err.message || 'Could not assign this delivery. Check the backend permissions and endpoint.');
    } finally {
      setAssigningDelivery('');
    }
  }

  async function updateOrder(order, action) {
    setBusyOrder(order.id);
    setError('');
    try {
      await api.post(`/order/${encodeURIComponent(order.id)}/${action}`);
      notify(`Order ${order.id} updated successfully.`);
      await load();
    } catch (err) {
      setError(err.message || 'Could not update this order.');
    } finally {
      setBusyOrder('');
    }
  }

  async function createStaff(event) {
    event.preventDefault();
    setStaffSaving(true);
    setError('');
    try {
      await api.post('/user/staff', staffForm);
      setStaffForm({ fullName: '', email: '', phone: '', password: '', role: 'PHARMACIST' });
      setCreatingStaff(false);
      notify(`${label(staffForm.role)} account created.`);
      await load();
    } catch (err) {
      setError(err.message || 'Could not create the staff account.');
    } finally {
      setStaffSaving(false);
    }
  }

  async function setUserActive(user) {
    setBusyUser(String(user.id));
    setError('');
    try {
      const active = !user.active;
      await api.patch(`/user/${encodeURIComponent(String(user.id))}/active?active=${active}`, {});
      notify(`${user.name} account ${active ? 'activated' : 'suspended'}.`);
      await load();
    } catch (err) {
      setError(err.message || 'Could not update the account status.');
    } finally {
      setBusyUser('');
    }
  }

  const navItems = [['Overview','▦'],['User Management','♙'],['Inventory','▤'],['Orders & Deliveries','↗'],['Audit Log','≡'],['Reports','▥']];
  return <div className="ad-app">
    <header className="ad-topbar">
      <button className="ad-brand" onClick={onExit}><span className="ad-mark">✚</span><span>PharmaFlow<small>Administration</small></span></button>
      <div className="ad-top-right"><span className="ad-secure">🔒 {currentUser?.fullName || 'Administrator'}</span><button className="ad-btn ad-ghost" onClick={onExit}>Exit workspace</button></div>
    </header>
    <div className="ad-layout">
      <aside className="ad-sidebar"><div className="ad-side-label">ADMINISTRATION</div>{navItems.map(([name,icon])=><button key={name} className={`ad-nav ${tab===name?'active':''}`} onClick={()=>{setTab(name);setQuery('');}}><span>{icon}</span>{name}{name==='Inventory'&&lowStock>0&&<b>{lowStock}</b>}</button>)}<div className="ad-side-note"><strong>Security reminder</strong><p>Use least-privilege access. Review unusual activity and avoid exposing patient information unnecessarily.</p></div></aside>
      <main className="ad-main">
        <div className="ad-page-head"><div><div className="ad-eyebrow">PHARMAFLOW · SYSTEM CONTROL</div><h1>{tab==='Overview'?'Administration overview':tab}</h1><p>Monitor pharmacy operations, manage access, and review system activity.</p></div><button className="ad-btn ad-ghost" onClick={load} disabled={loading}>{loading?'Refreshing…':'Refresh data'}</button></div>
        {notice&&<div className="ad-notice" role="status">{notice}</div>}
        {error&&<div className="ad-notice" role="alert">{error} <button className="ad-link" onClick={load}>Retry</button></div>}
        {tab==='Overview'&&<><section className="ad-stats">
          <article className="ad-stat"><span className="ad-stat-icon blue">♙</span><small>Registered users</small><strong>{users.length}</strong><label>Across all roles</label></article>
          <article className="ad-stat"><span className="ad-stat-icon amber">▤</span><small>Low-stock products</small><strong>{lowStock}</strong><label>At or below reorder level</label></article>
          <article className="ad-stat"><span className="ad-stat-icon blue">▣</span><small>Active orders</small><strong>{activeOrders.length}</strong><label>Not yet completed</label></article>
          <article className="ad-stat"><span className="ad-stat-icon green">✓</span><small>Completed orders</small><strong>{deliveredOrders.length}</strong><label>Delivered or collected</label></article>
        </section>
        <section className="ad-overview-grid"><article className="ad-panel"><div className="ad-panel-head"><div><h2>Inventory alerts</h2><p>Products at or below their reorder level</p></div><button className="ad-link" onClick={()=>setTab('Inventory')}>View inventory →</button></div>{activeInventory.filter(p=>p.stock<=p.reorder).map(p=><div className="ad-alert-row" key={p.id}><span className="ad-alert-icon"><img src={`/images/products/${String(p.id || '').toUpperCase()}.webp`} alt="" onError={event=>{event.currentTarget.style.display='none';}}/></span><div><strong>{p.name}</strong><small>{p.stock} units remaining · reorder at {p.reorder}</small></div><span className="ad-badge red">Low stock</span></div>)}{lowStock===0&&<p className="ad-muted">{inventoryLoading?'Loading inventory…':'No low-stock items.'}</p>}</article>
          <article className="ad-panel"><div className="ad-panel-head"><div><h2>Recent system activity</h2><p>Latest audit records from the backend</p></div><button className="ad-link" onClick={()=>setTab('Audit Log')}>View audit log →</button></div>{audit.slice(0,4).map(a=><div className="ad-activity" key={a.id}><span className="ad-activity-dot"/><div><strong>{a.action || 'System activity'}</strong><small>{a.actorRole || 'User'} #{a.actorId ?? '—'} · {a.entityTarget || '—'} {a.entityId || ''}</small></div><time>{dateTime(a.timeStamp)}</time></div>)}{!audit.length&&<p className="ad-muted">{loading?'Loading activity…':'No audit records returned.'}</p>}</article></section>
          <section className="ad-panel"><div className="ad-panel-head"><div><h2>Recent orders</h2><p>Live operational overview</p></div><button className="ad-link" onClick={()=>setTab('Orders & Deliveries')}>Manage orders →</button></div><OrderTable orders={orders} updateOrder={updateOrder} busyOrder={busyOrder} compact /></section></>}
        {tab==='User Management'&&<section className="ad-panel"><div className="ad-panel-head"><div><h2>User accounts</h2><p>Create staff logins and activate or suspend accounts.</p></div><div className="ad-action-group"><span className="ad-badge blue">{users.length} users</span><button className="ad-btn ad-primary" onClick={()=>setCreatingStaff(value=>!value)}>{creatingStaff?'Cancel':'Add staff account'}</button></div></div>{creatingStaff&&<form className="ad-staff-form" onSubmit={createStaff}><div className="ad-form-grid ad-two"><label>Full name<input required maxLength="120" value={staffForm.fullName} onChange={e=>setStaffForm({...staffForm,fullName:e.target.value})}/></label><label>Email<input required type="email" value={staffForm.email} onChange={e=>setStaffForm({...staffForm,email:e.target.value})}/></label><label>Phone<input value={staffForm.phone} onChange={e=>setStaffForm({...staffForm,phone:e.target.value})}/></label><label>Temporary password<input required type="password" minLength="8" maxLength="72" value={staffForm.password} onChange={e=>setStaffForm({...staffForm,password:e.target.value})}/></label><label>Staff role<select value={staffForm.role} onChange={e=>setStaffForm({...staffForm,role:e.target.value})}><option value="PHARMACIST">Pharmacist</option><option value="COURIER">Courier</option></select></label></div><div className="ad-modal-actions"><button type="submit" className="ad-btn ad-primary" disabled={staffSaving}>{staffSaving?'Creating…':'Create account'}</button></div></form>}<div className="ad-toolbar"><input placeholder="Search name, email, ID…" value={query} onChange={e=>setQuery(e.target.value)}/><select value={roleFilter} onChange={e=>setRoleFilter(e.target.value)}><option>All roles</option><option>Customer</option><option>Pharmacist</option><option>Courier</option><option>Admin</option></select></div><div className="ad-table-wrap"><table className="ad-table"><thead><tr><th>User</th><th>Email</th><th>Role</th><th>Account</th><th>Actions</th></tr></thead><tbody>{filteredUsers.map(u=><tr key={u.id}><td><strong>{u.name}</strong><small>{u.id}</small></td><td>{u.email}</td><td>{u.role}</td><td><span className={`ad-badge ${u.active?'green':'red'}`}>{u.status}</span></td><td><button className="ad-btn ad-small" disabled={busyUser===String(u.id) || u.id===currentUser?.id} onClick={()=>setUserActive(u)}>{busyUser===String(u.id)?'Saving…':u.active?'Suspend':'Activate'}</button></td></tr>)}</tbody></table>{!filteredUsers.length&&<div className="ad-empty">{loading?'Loading users…':'No users match this search.'}</div>}</div><p className="ad-footnote">Suspended users cannot log in, and their existing JWT sessions are rejected.</p></section>}
        {tab==='Inventory'&&<InventoryPanel inventory={inventory} categories={categories} loading={inventoryLoading} error={inventoryError} onReload={reloadInventory} notify={notify} money={money}/>}
        {tab==='Orders & Deliveries'&&<><section className="ad-panel"><div className="ad-panel-head"><div><h2>Courier assignment</h2><p>Assign packed delivery orders to a registered courier. Packing remains a pharmacist-only action.</p></div><button className="ad-btn ad-ghost" onClick={load} disabled={loading}>Refresh</button></div>
          {deliveryLoading ? <div className="ad-empty">Loading deliveries…</div> : <div className="ad-table-wrap"><table className="ad-table"><thead><tr><th>Delivery</th><th>Order</th><th>Address</th><th>Status</th><th>Assigned courier</th><th>Assign</th></tr></thead><tbody>{deliveries.map(d=>{const id=d.id ?? d.deliveryId; const status=String(d.status || d.deliveryStatus || '').toUpperCase(); const assigned=d.courierId ?? d.courier?.id; const eligible=['PACKED','PENDING','READY_FOR_DELIVERY','AWAITING_COURIER'].includes(status); return <tr key={id}><td><strong>{id ?? '—'}</strong></td><td>{d.order?.orderNumber ?? d.orderNumber ?? d.orderId ?? '—'}</td><td style={{whiteSpace:'normal',minWidth:180}}>{d.address || d.deliveryAddress || d.streetAddress || 'Address not supplied'}</td><td><span className={`ad-badge ${badge(status)}`}>{label(status)}</span></td><td>{assigned ? (users.find(u=>String(u.id)===String(assigned))?.name || `Courier #${assigned}`) : 'Unassigned'}</td><td><div className="ad-action-group"><select aria-label={`Choose courier for delivery ${id}`} value={courierChoices[String(id)] || ''} onChange={e=>setCourierChoices(old=>({...old,[String(id)]:e.target.value}))} disabled={!eligible || Boolean(assigned)}><option value="">Choose courier</option>{couriers.map(c=><option key={c.id} value={c.id}>{c.name} · #{c.id}</option>)}</select><button className="ad-btn ad-small" disabled={!eligible || Boolean(assigned) || !courierChoices[String(id)] || assigningDelivery===String(id)} onClick={()=>assignCourier(d)}>{assigningDelivery===String(id)?'Assigning…':'Assign delivery'}</button></div></td></tr>})}</tbody></table>{!deliveries.length&&<div className="ad-empty">No delivery records returned by the backend. Check GET /delivery/getall.</div>}</div>}
          {couriers.length===0&&<p className="ad-footnote">No courier accounts were returned by the users endpoint.</p>}</section><section className="ad-panel"><div className="ad-panel-head"><div><h2>Orders</h2><p>Admin order actions exclude packing; pharmacists pack orders.</p></div></div><OrderTable orders={orders} updateOrder={updateOrder} busyOrder={busyOrder}/></section></>}
        {tab==='Audit Log'&&<section className="ad-panel"><div className="ad-panel-head"><div><h2>Audit log</h2><p>Chronological record of stored actions.</p></div><span className="ad-badge blue">{audit.length} events</span></div><div className="ad-table-wrap"><table className="ad-table"><thead><tr><th>Time</th><th>Actor</th><th>Action</th><th>Target</th><th>Entity ID</th></tr></thead><tbody>{audit.map(a=><tr key={a.id}><td>{dateTime(a.timeStamp)}</td><td>{a.actorRole || '—'} #{a.actorId ?? '—'}</td><td>{a.action || '—'}</td><td>{a.entityTarget || '—'}</td><td>{a.entityId || '—'}</td></tr>)}</tbody></table>{!audit.length&&<div className="ad-empty">{loading?'Loading audit log…':'No audit records returned by the backend.'}</div>}</div></section>}
        {tab==='Reports'&&<section className="ad-report-grid"><article className="ad-panel"><h2>Order fulfilment</h2><p className="ad-muted">Current order distribution</p>{['PENDING','PROCESSING','PACKED','SHIPPED','DELIVERED','READY_FOR_COLLECTION','COLLECTED','CANCELLED'].map(s=><div className="ad-bar-row" key={s}><span>{label(s)}</span><div><i style={{width:`${(orders.length?orders.filter(o=>o.status===s).length/orders.length*100:0)}%`}}/></div><b>{orders.filter(o=>o.status===s).length}</b></div>)}</article><article className="ad-panel"><h2>User roles</h2><p className="ad-muted">Accounts by assigned role</p>{['Customer','Pharmacist','Courier','Admin'].map(s=><div className="ad-bar-row" key={s}><span>{s}</span><div><i style={{width:`${(users.length?users.filter(u=>u.role.toLowerCase()===s.toLowerCase()).length/users.length*100:0)}%`}}/></div><b>{users.filter(u=>u.role.toLowerCase()===s.toLowerCase()).length}</b></div>)}</article><article className="ad-panel"><h2>Stock health</h2><p className="ad-muted">{Math.max(activeInventory.length-lowStock,0)} products currently above reorder level.</p><div className="ad-report-number">{Math.round((activeInventory.length-lowStock)/Math.max(activeInventory.length,1)*100)}%</div><p className="ad-muted">Above reorder threshold</p></article></section>}
      </main>
    </div>
  </div>;
}

function OrderTable({orders,updateOrder,busyOrder,compact=false}) {
  const shown = compact ? orders.slice(0,5) : orders;
  return <div className="ad-table-wrap"><table className="ad-table"><thead><tr><th>Order</th><th>Customer ID</th><th>Total</th><th>Order status</th><th>Payment</th><th>Available actions</th></tr></thead><tbody>{shown.map(o=>{
    const busy=busyOrder===o.id;
    const actions=[];
    if(o.status==='PENDING' && o.paymentStatus==='PENDING') actions.push(['pay','Mark paid']);
    if(o.status==='READY_FOR_COLLECTION' && o.type==='CLICK_AND_COLLECT') actions.push(['collect','Mark collected']);
    if(!['DELIVERED','COLLECTED','CANCELLED'].includes(o.status)) actions.push(['cancel','Cancel']);
    return <tr key={o.id}><td><strong>{o.id}</strong><small>{dateTime(o.createdAt)}</small></td><td>{o.customer}</td><td>{money(o.total)}</td><td><span className={`ad-badge ${badge(o.status)}`}>{label(o.status)}</span></td><td><span className={`ad-badge ${badge(o.paymentStatus)}`}>{label(o.paymentStatus)}</span></td><td><div className="ad-action-group">{actions.map(([action,text])=><button key={action} className="ad-btn ad-small" disabled={busy} onClick={()=>updateOrder(o,action)}>{busy?'Saving…':text}</button>)}{!actions.length&&<span className="ad-muted">No actions</span>}</div></td></tr>;
  })}</tbody></table>{!shown.length&&<div className="ad-empty">No orders returned by the backend.</div>}</div>;
}
