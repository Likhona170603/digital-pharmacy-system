export default function PageTitle({ title, subtitle }) {
  return (
    <div className="ph-page-title">
      <div>
        <div className="ph-kicker">PHARMACIST WORKSPACE</div>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>
      <span className="ph-status-dot">
        <i /> Pharmacy workspace
      </span>
    </div>
  );
}
