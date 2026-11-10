import PageTitle from './PageTitle';
import ReviewPanel from './ReviewPanel';
import { formatDate, getPrescriptionStatus } from './pharmacistUtils';

export default function PrescriptionsTab({
  prescriptions,
  prescriptionsLoading,
  prescriptionsError,
  loadPrescriptions,
  visiblePrescriptions,
  pending,
  query,
  setQuery,
  filter,
  setFilter,
  badge,
  onReview,
  viewPrescriptionFile,
  busyPrescription,
  selected,
  reviewNote,
  setReviewNote,
  refills,
  setRefills,
  isApproving,
  setIsApproving,
  closeReview,
  review
}) {
  return (
    <>
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
            Verify the prescription, patient details, medicine, dosage and
            validity before approving. Prescription details are loaded from
            the backend.
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
            <p>{visiblePrescriptions.length} records shown</p>
          </div>
          <span className="ph-soft-label">{pending} awaiting action</span>
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
                        <strong>{prescription.prescriptionId}</strong>
                      </td>

                      <td>{prescription.patientId || '—'}</td>

                      <td>{prescription.productId || '—'}</td>

                      <td>{formatDate(prescription.issuedDate)}</td>

                      <td>{badge(status)}</td>

                      <td>
                        {prescription.filePath ? (
                          <button
                            type="button"
                            className="ph-link"
                            onClick={() => viewPrescriptionFile(prescription)}
                          >
                            View file ↗
                          </button>
                        ) : (
                          <span className="ph-muted">No file path</span>
                        )}
                      </td>

                      <td>
                        <button
                          className="ph-btn ph-btn-small"
                          onClick={() => onReview(prescription)}
                          disabled={
                            busyPrescription === prescription.prescriptionId
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

          {!prescriptionsLoading && visiblePrescriptions.length === 0 && (
            <div className="ph-empty">
              {prescriptionsError
                ? 'Prescriptions could not be loaded. Try refreshing.'
                : 'No prescriptions match these filters.'}
            </div>
          )}
        </div>
      </div>
      </section>
      <ReviewPanel
        selected={selected}
        busyPrescription={busyPrescription}
        reviewNote={reviewNote}
        setReviewNote={setReviewNote}
        refills={refills}
        setRefills={setRefills}
        isApproving={isApproving}
        setIsApproving={setIsApproving}
        onClose={closeReview}
        review={review}
        viewPrescriptionFile={viewPrescriptionFile}
      />
    </>
  );
}
