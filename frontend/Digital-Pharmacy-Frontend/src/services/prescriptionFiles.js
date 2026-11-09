import api from './api';

export async function openPrescriptionFile(prescriptionId) {
  if (!prescriptionId) throw new Error('Prescription ID is missing.');
  const preview = window.open('about:blank', '_blank');
  if (!preview) throw new Error('Allow pop-ups for PharmaFlow to open the prescription file.');
  preview.document.title = 'Opening prescription…';
  try {
    const blob = await api.download(`/prescription/${encodeURIComponent(String(prescriptionId))}/file`);
    const url = URL.createObjectURL(blob);
    preview.location.href = url;
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
  } catch (error) {
    preview.close();
    throw error;
  }
}
