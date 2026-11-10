import { useCallback, useEffect, useMemo, useState } from 'react';
import api from '../../services/api';
import { openPrescriptionFile } from '../../services/prescriptionFiles';
import { getPrescriptionStatus } from './pharmacistUtils';

export default function usePrescriptions({ query, filter, onNotify }) {
  const [prescriptions, setPrescriptions] = useState([]);
  const [prescriptionsLoading, setPrescriptionsLoading] = useState(false);
  const [prescriptionsError, setPrescriptionsError] = useState('');
  const [busyPrescription, setBusyPrescription] = useState('');
  const [selected, setSelected] = useState(null);
  const [reviewNote, setReviewNote] = useState('');
  const [refills, setRefills] = useState('1');
  const [isApproving, setIsApproving] = useState(false);
  const notify = text => onNotify?.(text);

  const pending = useMemo(
    () =>
      prescriptions.filter(prescription =>
        ['PENDING', 'NEEDS_CLARIFICATION'].includes(
          String(prescription.verificationStatus || '').toUpperCase()
        )
      ).length,
    [prescriptions]
  );

  const visiblePrescriptions = useMemo(() => {
    return prescriptions.filter(prescription => {
      const status = getPrescriptionStatus(
        prescription.verificationStatus
      );

      const searchText = [
        prescription.prescriptionId,
        prescription.patientId,
        prescription.productId,
        prescription.issuedDate,
        prescription.filePath,
        status
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();

      const matchesQuery = searchText.includes(query.toLowerCase());
      const matchesFilter = filter === 'All' || status === filter;

      return matchesQuery && matchesFilter;
    });
  }, [prescriptions, query, filter]);

  const loadPrescriptions = useCallback(async () => {
    setPrescriptionsLoading(true);
    setPrescriptionsError('');

    try {
      const data = await api.get('/prescription/pending');
      setPrescriptions(Array.isArray(data) ? data : []);
    } catch (err) {
      setPrescriptionsError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to view prescriptions. Log in with a pharmacist account.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not load prescriptions.'
      );
    } finally {
      setPrescriptionsLoading(false);
    }
  }, []);

  const openReview = prescription => {
    setSelected(prescription);
    setReviewNote(prescription.rejectionReason || '');
    setRefills('1');
    setIsApproving(false);
  };

  const closeReview = () => {
    setSelected(null);
    setRefills('1');
    setIsApproving(false);
  };

  const viewPrescriptionFile = async prescription => {
    try {
      await openPrescriptionFile(
        prescription?.prescriptionId || prescription?.id
      );
    } catch (error) {
      notify(error.message || 'Could not open this prescription file.');
    }
  };

  const review = async (item, action) => {
    const prescriptionId = item?.prescriptionId;

    if (!prescriptionId) {
      notify('This prescription has no ID and cannot be updated.');
      return;
    }

    const currentStatus = String(
      item.verificationStatus || ''
    ).toUpperCase();

    if (!['PENDING', 'NEEDS_CLARIFICATION'].includes(currentStatus)) {
      notify('This prescription is no longer awaiting review.');
      return;
    }

    if (['reject', 'clarify'].includes(action) && !reviewNote.trim()) {
      notify(
        action === 'reject'
          ? 'Enter a reason before rejecting the prescription.'
          : 'Enter a reason explaining what information is needed.'
      );
      return;
    }

    const refillCount = Number(refills);

    if (
      action === 'approve' &&
      (!String(refills).trim() ||
        !Number.isInteger(refillCount) ||
        refillCount < 0 ||
        refillCount > 12)
    ) {
      notify('Enter a whole number of refills between 0 and 12.');
      return;
    }

    setBusyPrescription(prescriptionId);
    setPrescriptionsError('');

    try {
      const id = encodeURIComponent(String(prescriptionId));
      let endpoint;

      if (action === 'approve') {
        endpoint =
          `/prescription/${id}/approve` +
          `?refills=${refillCount}`;
      } else {
        const reason = encodeURIComponent(reviewNote.trim());
        const actionPath = action === 'reject' ? 'reject' : 'clarify';

        endpoint =
          `/prescription/${id}/${actionPath}` +
          `?reason=${reason}`;
      }

      await api.put(endpoint, {});

      const successMessage = {
        approve: 'Prescription approved.',
        reject: 'Prescription rejected.',
        clarify: 'Clarification requested.'
      };

      notify(`${prescriptionId}: ${successMessage[action]}`);

      setSelected(null);
      setReviewNote('');
      setRefills('1');
      setIsApproving(false);

      await loadPrescriptions();
    } catch (err) {
      const message =
        err.status === 401 || err.status === 403
          ? 'You are not authorised to review prescriptions. Check your pharmacist login.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'The prescription decision could not be saved.';

      setPrescriptionsError(message);
      notify(message);
    } finally {
      setBusyPrescription('');
    }
  };

  useEffect(() => {
    loadPrescriptions();
  }, [loadPrescriptions]);

  return {
    prescriptions,
    prescriptionsLoading,
    prescriptionsError,
    setPrescriptionsError,
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
  };
}
