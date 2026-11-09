import React, { useState } from 'react';
import PasswordField from '../components/PasswordField';
import api from '../services/api';
import '../styles/auth.css';


export default function Login({ onRegister }) {
  const [form, setForm] = useState({
    email: '',
    password: '',
    remember: false
  });
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const set = e => {
    const { name, value, type, checked } = e.target;

    setForm(previous => ({
      ...previous,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const submit = async e => {
    e.preventDefault();
    setMessage('');
    setSuccess(false);

    if (!form.email.trim() || !form.password) {
      setMessage('Please enter your email address and password.');
      return;
    }

    setLoading(true);

    try {
      const data = await api.post('/auth/login', {
        email: form.email.trim().toLowerCase(),
        password: form.password
      });

      if (!data.token) {
        throw new Error('Login succeeded, but the server did not return an authentication token.');
      }

      localStorage.removeItem('pharmaflow_token');
      localStorage.removeItem('pharmaflow_user');
      sessionStorage.removeItem('pharmaflow_token');
      sessionStorage.removeItem('pharmaflow_user');
      const storage = form.remember ? localStorage : sessionStorage;

      storage.setItem('pharmaflow_token', data.token);
      const user = {
        id: data.user.id,
        role: String(data.user.role || '').toUpperCase(),
        fullName: data.user.fullName || '',
        email: data.user.email || form.email.trim().toLowerCase(),
        phone: data.user.phone || ''
      };
      storage.setItem('pharmaflow_user', JSON.stringify(user));

      setSuccess(true);
      setMessage('Login successful!');

      window.dispatchEvent(
        new CustomEvent('pharmaflow-login', {
          detail: user
        })
      );
    } catch (error) {
      setMessage(error.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="au">
      <div className="au-wrap">
        <aside className="au-side">
          <span className="hp-mark">✚</span>
          <h2>Welcome to your PharmaFlow workspace.</h2>
          <ul>
            <li>Secure access to your assigned workspace</li>
            <li>Manage pharmacy operations and requests</li>
            <li>Keep patient and product information protected</li>
          </ul>
        </aside>

        <form className="au-card" onSubmit={submit}>
          <h1>Welcome back</h1>
          <p className="au-sub">
            Sign in to access your authorised PharmaFlow workspace.
          </p>

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

          <PasswordField
            id="password"
            label="Password"
            value={form.password}
            onChange={e =>
              setForm(previous => ({
                ...previous,
                password: e.target.value
              }))
            }
            placeholder="Enter your password"
            autoComplete="current-password"
          />

          <div className="au-row">
            <label className="au-check">
              <input
                type="checkbox"
                name="remember"
                checked={form.remember}
                onChange={set}
              />
              Remember me
            </label>

            <button
              type="button"
              className="hp-text-btn"
              onClick={() =>
                setMessage(
                  'Password reset is not available yet. Please contact the administrator.'
                )
              }
            >
              Forgot password?
            </button>
          </div>

      {message && (
  <div
    className={success ? 'au-message au-success' : 'au-message au-error'}
    role="status"
  >
    {message}
  </div>
)}

          <button
            className="hp-btn hp-btn-primary hp-btn-lg au-submit"
            type="submit"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>

          <p className="au-switch">
            New to PharmaFlow?{' '}
            <button
              type="button"
              className="hp-text-btn"
              onClick={onRegister}
            >
              Create an account
            </button>
          </p>

          <p className="au-secure">
            🔒 Your details are encrypted and kept private.
          </p>

        </form>
      </div>
    </div>
  );
}