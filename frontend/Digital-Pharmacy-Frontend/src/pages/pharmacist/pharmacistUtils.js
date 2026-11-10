const STATUS = {
  PENDING: 'Pending review',
  APPROVED: 'Verified',
  REJECTED: 'Rejected',
  NEEDS_CLARIFICATION: 'Needs information'
};

const formatDate = value => {
  if (!value) return '—';

  try {
    const date = Array.isArray(value)
      ? new Date(value[0], (value[1] || 1) - 1, value[2] || 1)
      : new Date(value);

    return Number.isNaN(date.getTime())
      ? String(value)
      : date.toLocaleString([], {
          dateStyle: 'medium',
          timeStyle: 'short'
        });
  } catch {
    return String(value);
  }
};

const getPrescriptionStatus = value =>
  STATUS[String(value || '').toUpperCase()] || String(value || 'Unknown');

export { formatDate, getPrescriptionStatus };
