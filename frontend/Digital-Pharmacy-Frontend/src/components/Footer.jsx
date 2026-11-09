import React from 'react';
import '../styles/footer.css';

export default function Footer({ onHome, onAbout, onContact, onLogin, onRegister, onBrowse }) {
  return <footer className="hp-footer">
    <div className="hp-foot-grid">
      <div>
        <a className="hp-brand" href="/" onClick={e => { e.preventDefault(); onHome?.(); }}><span className="hp-mark">✚</span><span>Pharma<b>Flow</b></span></a>
        <p>An online pharmacy for everyday health, prescriptions and delivery.</p>
      </div>
      <div><h4>Shop</h4><button onClick={() => onBrowse?.()}>All products</button><button onClick={() => onBrowse?.('Vitamins')}>Vitamins</button><button onClick={() => onBrowse?.('Chronic care')}>Chronic care</button></div>
      <div><h4>Account</h4><button onClick={onLogin}>Sign in</button><button onClick={onRegister}>Create account</button><button onClick={onLogin}>My orders</button></div>
      <div><h4>Company</h4><button onClick={onAbout}>About us</button><button onClick={onContact}>Contact us</button><span>support@pharmaflow.co.za</span><span>Mon–Sat, 8:00–19:00</span><span>Cape Town, South Africa</span></div>
    </div>
    <div className="hp-legal">
      <span>© {new Date().getFullYear()} PharmaFlow. Always read the label and follow the directions for use.</span>
      <span>Scheduled medicines are dispensed only against a valid prescription.</span>
    </div>
  </footer>;
}
