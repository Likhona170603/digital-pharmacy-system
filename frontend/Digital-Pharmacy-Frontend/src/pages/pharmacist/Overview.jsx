import PageTitle from './PageTitle';
import Stat from './Stat';
import { formatDate, getPrescriptionStatus } from './pharmacistUtils';

export default function Overview({
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
                    getPrescriptionStatus(prescription.verificationStatus)
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
