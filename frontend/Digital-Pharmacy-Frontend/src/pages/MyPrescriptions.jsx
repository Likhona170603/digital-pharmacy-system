import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { openPrescriptionFile } from '../services/prescriptionFiles';
import '../styles/prescriptions.css';

async function uploadPrescription(formData) { return api.upload('/prescription/upload', formData); }

function getProductId(product) {
  return String(product?.productId ?? product?.id ?? '');
}

function getProductName(product) {
  return product?.productName || product?.name || `Medicine ${getProductId(product)}`;
}

function getStatusLabel(status) {
  if (status === 'APPROVED') return 'Approved';
  if (status === 'REJECTED') return 'Rejected';
  if (status === 'NEEDS_CLARIFICATION') return 'Needs clarification';
  return 'Pending review';
}

function getStatusColor(status) {
  if (status === 'APPROVED') return '#15803d';
  if (status === 'REJECTED') return '#b91c1c';
  if (status === 'NEEDS_CLARIFICATION') return '#a16207';
  return '#0f47a8';
}

export default function MyPrescriptions({ onNotify }) {
  const [products, setProducts] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [productId, setProductId] = useState('');
  const [issuedDate, setIssuedDate] = useState('');
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [openingFile, setOpeningFile] = useState('');

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      const [medicineData, prescriptionData] = await Promise.all([
        api.get('/product/rx'),
        api.get('/prescription/mine')
      ]);
      const medicineList = Array.isArray(medicineData) ? medicineData : medicineData?.content || [];
      const prescriptionList = Array.isArray(prescriptionData) ? prescriptionData : [];
      setProducts(medicineList);
      setPrescriptions(prescriptionList);
      setProductId(current => current && medicineList.some(item => getProductId(item) === current) ? current : getProductId(medicineList[0]));
    } catch (err) {
      setError(err.status === 401 ? 'Please log in to view your prescriptions.' : err.message || 'Could not load your prescriptions.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadData(); }, []);

  async function handleUpload(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    if (!(localStorage.getItem('pharmaflow_token') || sessionStorage.getItem('pharmaflow_token'))) return setError('Please log in before uploading a prescription.');
    if (!productId) return setError('Please select a medicine.');
    if (!file) return setError('Please select a prescription file.');
    if (!['application/pdf', 'image/jpeg', 'image/png'].includes(file.type)) return setError('Choose a PDF, JPG or PNG file.');
    if (file.size > 5 * 1024 * 1024) return setError('The file must be 5 MB or smaller.');

    const formData = new FormData();
    formData.append('file', file);
    formData.append('productId', productId);
    if (issuedDate) formData.append('issuedDate', issuedDate);

    setUploading(true);
    try {
      await uploadPrescription(formData);
      setMessage('Prescription uploaded. It is awaiting pharmacist review.');
      setFile(null);
      setIssuedDate('');
      const input = document.getElementById('prescription-file');
      if (input) input.value = '';
      await loadData();
      onNotify?.('Prescription uploaded successfully.');
    } catch (err) {
      setError(err.message || 'Could not upload your prescription.');
    } finally {
      setUploading(false);
    }
  }

  if (loading) return <main className="prescriptions-page"><h1>My Prescriptions</h1><p>Loading your prescriptions…</p></main>;

  return (
    <main className="prescriptions-page">
      <header className="prescriptions-header">
        <p className="prescriptions-eyebrow">YOUR HEALTH RECORDS</p>
        <h1>My Prescriptions</h1>
        <p>Upload your prescription and follow its progress while our pharmacist reviews it.</p>
      </header>

      {error && <div className="prescription-alert prescription-alert-error" role="alert">{error}</div>}
      {message && <div className="prescription-alert prescription-alert-success" role="status">{message}</div>}

      <section className="prescription-panel">
        <h2>Upload a prescription</h2>
        <p className="prescription-muted">Choose the medicine covered by your prescription. Accepted files are PDF, JPG and PNG, up to 5 MB.</p>
        <form onSubmit={handleUpload} className="prescription-form">
          <label htmlFor="prescription-product">Medicine</label>
          <select id="prescription-product" value={productId} onChange={event => setProductId(event.target.value)} required disabled={uploading || products.length === 0}>
            <option value="">{products.length ? 'Select a medicine' : 'No prescription medicines available'}</option>
            {products.map(product => <option key={getProductId(product)} value={getProductId(product)}>{getProductName(product)}</option>)}
          </select>
          <label htmlFor="prescription-date">Prescription issue date (optional)</label>
          <input id="prescription-date" type="date" value={issuedDate} max={new Date().toLocaleDateString('en-CA')} onChange={event => setIssuedDate(event.target.value)} disabled={uploading} />
          <label htmlFor="prescription-file">Prescription file</label>
          <input id="prescription-file" type="file" accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png" onChange={event => setFile(event.target.files?.[0] || null)} required disabled={uploading} />
          {file && <p className="prescription-muted">Selected: {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)</p>}
          <button className="hp-btn hp-btn-primary" type="submit" disabled={uploading || products.length === 0}>{uploading ? 'Uploading…' : 'Upload prescription'}</button>
        </form>
      </section>

      <section className="prescription-list-section">
        <div className="prescription-list-heading">
          <div><h2>Your uploaded prescriptions</h2><p className="prescription-muted">Check the review status of each submission.</p></div>
          <button type="button" className="hp-btn hp-btn-ghost" onClick={loadData}>Refresh</button>
        </div>
        {prescriptions.length === 0 ? (
          <div className="prescription-empty"><span aria-hidden="true">📄</span><h3>No prescriptions uploaded yet</h3><p>Your uploaded prescriptions will appear here.</p></div>
        ) : (
          <div className="prescription-list">
            {prescriptions.map(prescription => {
              const status = prescription.verificationStatus;
              const medicine = products.find(product => getProductId(product) === String(prescription.productId));
              const reason = prescription.rejectionReason || prescription.clarificationReason || '';
              return (
                <article className="prescription-card" key={prescription.prescriptionId || prescription.id}>
                  <div className="prescription-card-top">
                    <div><h3>{medicine ? getProductName(medicine) : `Medicine ${prescription.productId || ''}`}</h3><p className="prescription-muted">Prescription #{prescription.prescriptionId || prescription.id}{prescription.issuedDate ? ` · Issued ${prescription.issuedDate}` : ''}</p></div>
                    <span className="prescription-status" style={{ color: getStatusColor(status), background: `${getStatusColor(status)}14` }}>{getStatusLabel(status)}</span>
                  </div>
                  {status === 'REJECTED' && <div className="prescription-feedback"><strong>Pharmacist's reason</strong><p>{reason || 'Please contact the pharmacy for more information.'}</p></div>}
                  {status === 'NEEDS_CLARIFICATION' && <div className="prescription-feedback"><strong>Clarification requested</strong><p>{reason || 'Please contact the pharmacy to confirm what information is needed.'}</p></div>}
                  {prescription.filePath && <p><button type="button" className="hp-btn hp-btn-ghost" disabled={openingFile===String(prescription.prescriptionId || prescription.id)} onClick={async () => {
                    const id = String(prescription.prescriptionId || prescription.id);
                    setOpeningFile(id);
                    setError('');
                    try { await openPrescriptionFile(id); } catch (err) { setError(err.message || 'Could not open prescription file.'); }
                    finally { setOpeningFile(''); }
                  }}>{openingFile===String(prescription.prescriptionId || prescription.id) ? 'Opening…' : 'View uploaded file'}</button></p>}
                  {status === 'APPROVED' && <p className="prescription-approved-note">Your prescription has been approved. Its validity and available refills will be checked when you place an order.</p>}
                  {(status === 'REJECTED' || status === 'NEEDS_CLARIFICATION') && <p className="prescription-muted">You can submit an updated prescription using the upload form above.</p>}
                </article>
              );
            })}
          </div>
        )}
      </section>
    </main>
  );
}
