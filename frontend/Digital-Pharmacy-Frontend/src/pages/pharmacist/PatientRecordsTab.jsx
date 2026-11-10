import PageTitle from './PageTitle';

export default function PatientRecordsTab() {
  return (
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
            authentication and permission checks. This backend integration
            does not yet provide a confirmed patient records endpoint.
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
          Patient records will appear here when a secure patient records API
          is implemented.
        </div>
      </div>
    </section>
  );
}
