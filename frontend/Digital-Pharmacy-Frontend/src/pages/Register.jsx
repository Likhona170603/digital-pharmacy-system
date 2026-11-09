
import React, { useState } from 'react';
import PasswordField from '../components/PasswordField';
import api from '../services/api';
import '../styles/auth.css';


export default function Register({ onLogin }) {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    confirm: '',
    terms: false
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const set = e => {
    const { name, value, type, checked } = e.target;

    setForm(previous => ({
      ...previous,
      [name]: type === 'checkbox' ? checked : value
    }));

    setError('');
    setDone(false);
  };

  const mismatch = form.confirm && form.password !== form.confirm;

  const submit = async e => {
    e.preventDefault();
    setError('');
    setDone(false);

    if (form.password !== form.confirm) {
      setError('Passwords do not match.');
      return;
    }

    if (form.password.length < 8 || form.password.length > 72) {
      setError('Password must contain between 8 and 72 characters.');
      return;
    }

    if (!form.terms) {
      setError('Please accept the terms to continue.');
      return;
    }

    setLoading(true);

    try {
      await api.post('/auth/register', {
        fullName: form.fullName.trim(),
        email: form.email.trim().toLowerCase(),
        phone: form.phone.trim() || null,
        password: form.password
      });

      setDone(true);
      setForm({
        fullName: '',
        email: '',
        phone: '',
        password: '',
        confirm: '',
        terms: false
      });
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="au">
      <div className="au-wrap">
        <aside className="au-side">
          <span className="hp-mark">✚</span>
          <h2>Healthcare that is easier to manage.</h2>
          <ul>
            <li>Order medicine and health essentials online</li>
            <li>Upload prescriptions for pharmacist approval</li>
            <li>Follow every order from packing to delivery</li>
          </ul>
        </aside>

        <form className="au-card" onSubmit={submit}>
          <h1>Create your account</h1>
          <p className="au-sub">
            Join PharmaFlow for a simpler healthcare experience.
          </p>

          <div className="au-field">
            <label htmlFor="fullName">Full name</label>
            <input
              id="fullName"
              name="fullName"
              value={form.fullName}
              onChange={set}
              placeholder="Enter your full name"
              autoComplete="name"
              maxLength={120}
              required
            />
          </div>

          <div className="au-two">
            <div className="au-field">
              <label htmlFor="email">Email address</label>
              <input
                id="email"
                name="email"
                type="email"
                value={form.email}
                onChange={set}
                placeholder="you@example.com"
                autoComplete="email"
                required
              />
            </div>

            <div className="au-field">
              <label htmlFor="phone">
                Phone number <span className="au-opt">(optional)</span>
              </label>
              <input
                id="phone"
                name="phone"
                type="tel"
                value={form.phone}
                onChange={set}
                placeholder="082 123 4567"
                autoComplete="tel"
              />
            </div>
          </div>

          <PasswordField
            id="password"
            label="Password"
            value={form.password}
            onChange={e => {
              setForm(previous => ({ ...previous, password: e.target.value }));
              setError('');
              setDone(false);
            }}
            placeholder="At least 8 characters"
            autoComplete="new-password"
          />

          <PasswordField
            id="confirm"
            label="Confirm password"
            value={form.confirm}
            onChange={e => {
              setForm(previous => ({ ...previous, confirm: e.target.value }));
              setError('');
              setDone(false);
            }}
            placeholder="Repeat your password"
            autoComplete="new-password"
            hint={mismatch ? 'Passwords do not match.' : ''}
          />

          <label className="au-check au-terms">
            <input
              type="checkbox"
              name="terms"
              checked={form.terms}
              onChange={set}
            />
            I agree to the Terms of Use and Privacy Policy, and consent to
            PharmaFlow processing my health information to provide pharmacy
            services.
          </label>

          <p className="au-staff">
            Staff accounts (pharmacists, couriers and administrators) are
            created by a PharmaFlow administrator.
          </p>

          {error && (
            <div className="au-message au-error" role="alert">
              {error}
            </div>
          )}

          {done && (
            <div className="au-message" role="status">
              Account created successfully! You can now sign in.
            </div>
          )}

          <button
            className="hp-btn hp-btn-primary hp-btn-lg au-submit"
            type="submit"
            disabled={loading}
          >
            {loading ? 'Creating account…' : 'Create account'}
          </button>

          <p className="au-switch">
            Already have an account?{' '}
            <button
              type="button"
              className="hp-text-btn"
              onClick={onLogin}
            >
              Sign in
            </button>
          </p>
        </form>
      </div>
    </div>
  );
}