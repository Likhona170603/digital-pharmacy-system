
import React, { useEffect, useState } from 'react';
import '../styles/auth.css';
import api from '../services/api';


export default function Profile({ user, onHome, onNotify }) {
  const [form, setForm] = useState({
    fullName: user?.fullName || '',
    email: user?.email || '',
    phone: user?.phone || ''
  });

  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState(false);

  useEffect(() => {
    setForm({
      fullName: user?.fullName || '',
      email: user?.email || '',
      phone: user?.phone || ''
    });
    setMessage('');
    setError(false);
  }, [user]);

  const handleChange = event => {
    setForm(previous => ({
      ...previous,
      [event.target.name]: event.target.value
    }));
  };

  const handleSubmit = async event => {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    setError(false);

    try {
      if (!user?.id) {
        throw new Error('Your user ID is missing. Please log in again.');
      }

      const updatedUser = await api.post('/user/update', {
        id: user.id,
        fullName: form.fullName.trim(),
        phone: form.phone.trim() || null
      });


      const updatedProfile = {
        ...user,
        fullName: updatedUser.fullName,
        email: updatedUser.email,
        phone: updatedUser.phone,
        role: updatedUser.role
      };

      const storage = localStorage.getItem('pharmaflow_user')
        ? localStorage
        : sessionStorage;

      storage.setItem('pharmaflow_user', JSON.stringify(updatedProfile));
      window.dispatchEvent(new CustomEvent('pharmaflow-user-updated', { detail: updatedProfile }));

      setForm({
        fullName: updatedProfile.fullName || '',
        email: updatedProfile.email || '',
        phone: updatedProfile.phone || ''
      });

      setMessage('Profile updated successfully.');
      onNotify?.('Your profile has been saved.');
    } catch (err) {
      setError(true);
      setMessage(err.message || 'Something went wrong while saving your profile.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section
      style={{
        maxWidth: 900,
        margin: '0 auto',
        padding: '48px 24px 64px'
      }}
    >
      <button
        type="button"
        className="hp-text-btn"
        onClick={onHome}
        style={{ marginBottom: 24 }}
      >
        ← Back to home
      </button>

      <div
        style={{
          background: '#fff',
          border: '1px solid #d9e6f7',
          borderRadius: 16,
          padding: 32
        }}
      >
        <h1 style={{ marginTop: 0 }}>My Profile</h1>
        <p style={{ color: '#566b82', marginBottom: 28 }}>
          View and manage your PharmaFlow account details.
        </p>

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 16,
            marginBottom: 32
          }}
        >
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: '50%',
              background: '#e8f2ff',
              color: '#174a83',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 22,
              fontWeight: 700
            }}
          >
            {(form.fullName || form.email || 'U')
              .split(/\s+/)
              .filter(Boolean)
              .slice(0, 2)
              .map(part => part[0].toUpperCase())
              .join('')}
          </div>

          <div>
            <h2 style={{ margin: '0 0 5px' }}>
              {form.fullName || 'Your name'}
            </h2>
            <p style={{ margin: 0, color: '#566b82' }}>
              {user?.role || 'Customer'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="au-field">
            <label htmlFor="profile-fullName">Full name</label>
            <input
              id="profile-fullName"
              name="fullName"
              value={form.fullName}
              onChange={handleChange}
              autoComplete="name"
              required
              maxLength={120}
            />
          </div>

          <div className="au-field">
            <label htmlFor="profile-email">Email address</label>
            <input
              id="profile-email"
              name="email"
              type="email"
              value={form.email}
              autoComplete="email"
              disabled
            />
            <small style={{ color: '#566b82' }}>
              Email changes are not available here yet.
            </small>
          </div>

          <div className="au-field">
            <label htmlFor="profile-phone">Phone number</label>
            <input
              id="profile-phone"
              name="phone"
              type="tel"
              value={form.phone}
              onChange={handleChange}
              autoComplete="tel"
              placeholder="Enter your phone number"
            />
          </div>

          {message && (
            <div
              className="au-message"
              role="status"
              style={{ color: error ? '#b42318' : '#167647' }}
            >
              {message}
            </div>
          )}

          <button
            className="hp-btn hp-btn-primary hp-btn-lg"
            type="submit"
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save changes'}
          </button>
        </form>
      </div>
    </section>
  );
}