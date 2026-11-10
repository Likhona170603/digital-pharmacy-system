import React, { useMemo, useState } from 'react';
import useInventory from '../hooks/useInventory';
import '../styles/pharmacist.css';
import Overview from './pharmacist/Overview';
import usePrescriptions from './pharmacist/usePrescriptions';
import useOrders from './pharmacist/useOrders';
import PrescriptionsTab from './pharmacist/PrescriptionsTab';
import OrdersTab from './pharmacist/OrdersTab';
import StockTab from './pharmacist/StockTab';
import PatientRecordsTab from './pharmacist/PatientRecordsTab';

export default function PharmacistDashboard({ onNotify, onHome }) {
  const [tab, setTab] = useState('Overview');
  const notify = text => onNotify?.(text);

  const {
    orders,
    ordersLoading,
    ordersError,
    loadOrders,
    busyOrder,
    packOrder,
    collectOrder
  } = useOrders({ onNotify });

  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState('All');
  const {
    prescriptions,
    prescriptionsLoading,
    prescriptionsError,
    pending,
    visiblePrescriptions,
    loadPrescriptions,
    busyPrescription,
    selected,
    reviewNote,
    setReviewNote,
    refills,
    setRefills,
    isApproving,
    setIsApproving,
    openReview,
    closeReview,
    viewPrescriptionFile,
    review
  } = usePrescriptions({ query, filter, onNotify });

  const {
    inventory,
    loading: inventoryLoading,
    error: inventoryError,
    reload: reloadInventory
  } = useInventory();

  const lowStock = useMemo(
    () =>
      inventory
        .filter(product => product.active && product.stock <= product.reorder)
        .sort((a, b) => a.stock - b.stock),
    [inventory]
  );

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
            <PrescriptionsTab
              prescriptions={prescriptions}
              prescriptionsLoading={prescriptionsLoading}
              prescriptionsError={prescriptionsError}
              loadPrescriptions={loadPrescriptions}
              visiblePrescriptions={visiblePrescriptions}
              pending={pending}
              query={query}
              setQuery={setQuery}
              filter={filter}
              setFilter={setFilter}
              badge={badge}
              onReview={openReview}
              viewPrescriptionFile={viewPrescriptionFile}
              busyPrescription={busyPrescription}
              selected={selected}
              reviewNote={reviewNote}
              setReviewNote={setReviewNote}
              refills={refills}
              setRefills={setRefills}
              isApproving={isApproving}
              setIsApproving={setIsApproving}
              closeReview={closeReview}
              review={review}
            />
          )}

          {tab === 'Prepare Orders' && (
            <OrdersTab
              orders={orders}
              ordersLoading={ordersLoading}
              ordersError={ordersError}
              loadOrders={loadOrders}
              packOrder={packOrder}
              collectOrder={collectOrder}
              busyOrder={busyOrder}
              badge={badge}
            />
          )}

          {tab === 'Product Catalogue' && (
            <StockTab
              inventory={inventory}
              inventoryLoading={inventoryLoading}
              inventoryError={inventoryError}
              reloadInventory={reloadInventory}
              query={query}
              setQuery={setQuery}
              badge={badge}
              notify={notify}
            />
          )}

          {tab === 'Patient Records' && <PatientRecordsTab />}
        </main>
      </div>

    </div>
  );
}
