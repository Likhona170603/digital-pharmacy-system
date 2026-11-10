import { formatDate, getPrescriptionStatus } from './pharmacistUtils';

export default function ReviewPanel({
  selected,
  busyPrescription,
  reviewNote,
  setReviewNote,
  refills,
  setRefills,
  isApproving,
  setIsApproving,
  onClose,
  review,
  viewPrescriptionFile
}) {
  if (!selected) return null;

  return (
    <div
      className="ph-modal-backdrop"
      onMouseDown={event => {
        if (event.target === event.currentTarget && !busyPrescription) {
          onClose();
        }
      }}
    >
      <section
        className="ph-review-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="review-title"
      >
        <button
          className="ph-modal-close"
          onClick={onClose}
          aria-label="Close"
          disabled={Boolean(busyPrescription)}
        >
          ×
        </button>

        <span className="ph-soft-label">PRESCRIPTION REVIEW</span>

        <h2 id="review-title">{selected.prescriptionId}</h2>

        <p className="ph-muted">Issued {formatDate(selected.issuedDate)}</p>

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
              <button
                type="button"
                className="ph-link"
                onClick={() => viewPrescriptionFile(selected)}
              >
                Open prescription ↗
              </button>
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
              Inspect the prescription and verify the relevant details. A
              product ID alone does not establish the medicine, dosage, or
              clinical appropriateness.
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

        {isApproving && (
          <>
            <label className="ph-note-label" htmlFor="review-refills">
              Refills
            </label>
            <input
              id="review-refills"
              className="ph-number-input"
              type="number"
              min="0"
              max="12"
              step="1"
              value={refills}
              onChange={event => setRefills(event.target.value)}
              disabled={Boolean(busyPrescription)}
            />
          </>
        )}

        <div className="ph-modal-actions">
          {isApproving ? (
            <>
              <button
                type="button"
                className="ph-btn ph-btn-outline"
                onClick={() => setIsApproving(false)}
                disabled={Boolean(busyPrescription)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="ph-btn ph-btn-primary"
                onClick={() => review(selected, 'approve')}
                disabled={Boolean(busyPrescription)}
              >
                {busyPrescription === selected.prescriptionId
                  ? 'Saving…'
                  : 'Confirm approval'}
              </button>
            </>
          ) : (
            <>
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
                onClick={() => setIsApproving(true)}
                disabled={Boolean(busyPrescription)}
              >
                Approve prescription
              </button>
            </>
          )}
        </div>

        <p className="ph-modal-footnote">
          Decisions are submitted to the Spring Boot backend.
        </p>
      </section>
    </div>
  );
}
