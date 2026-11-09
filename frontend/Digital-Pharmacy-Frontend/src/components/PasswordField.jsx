import React, { useState } from 'react';

export default function PasswordField({ id, label, value, onChange, placeholder, autoComplete, hint }) {
  const [show, setShow] = useState(false);
  return <div className="au-field">
    <label htmlFor={id}>{label}</label>
    <div className="au-pass">
      <input id={id} type={show ? 'text' : 'password'} value={value} onChange={onChange} placeholder={placeholder} autoComplete={autoComplete} required minLength={6} />
      <button type="button" onClick={() => setShow(s => !s)} aria-pressed={show}>{show ? 'Hide' : 'Show'}</button>
    </div>
    {hint && <span className="au-hint">{hint}</span>}
  </div>;
}
