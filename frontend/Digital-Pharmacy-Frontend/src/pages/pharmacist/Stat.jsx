export default function Stat({ icon, label, value, note, tone, onClick }) {
  return (
    <button className="ph-stat-card" onClick={onClick}>
      <span className={`ph-stat-icon ${tone}`}>{icon}</span>
      <span className="ph-stat-label">{label}</span>
      <strong>{value}</strong>
      <small>{note}</small>
      <span className="ph-stat-open">Open section ↗</span>
    </button>
  );
}
