import React from 'react';
import '../styles/about.css';

const values = [
  { icon: '♡', title: 'People first', text: 'Clear information and simple user journeys.' },
  { icon: '⌕', title: 'Accessible browsing', text: 'Organised catalogue and category navigation.' },
  { icon: '▤', title: 'Connected workflows', text: 'Prescriptions and orders in one experience.' },
  { icon: '✓', title: 'Transparency', text: 'Understand the status of key activities.' }
];

const facts = [
  { value: '7', label: 'product categories, from pain relief to chronic care' },
  { value: 'Rx + OTC', label: 'every product is tagged, so you know what needs a script' },
  { value: '4 stages', label: 'from payment to packing to delivery, visible in your account' },
  { value: 'Mon–Sat', label: 'a registered pharmacist on duty, 8:00 to 19:00' }
];

const journey = [
  { who: 'You', title: 'Place your order', text: 'Browse the catalogue, add items to your cart and upload a prescription if one is needed.' },
  { who: 'Pharmacist', title: 'Verify the script', text: 'A registered pharmacist checks each prescription, then approves it, rejects it or asks you to clarify.' },
  { who: 'Pharmacy team', title: 'Pack and seal', text: 'Approved orders are picked, checked against the script and packed for delivery or collection.' },
  { who: 'Courier', title: 'Deliver to you', text: 'Your courier updates the delivery status and records who received the parcel.' }
];

const standards = [
  'Scheduled medicines are only released against a pharmacist-approved prescription.',
  'Stock levels are checked before an order is accepted, so you are not sold what we do not have.',
  'Every prescription decision and order change is recorded in an audit log.',
  'Your health details are kept private and shared only with the staff who need them to serve you.'
];

export default function About({ onRegister, onBrowse }) {
  return <div className="ab">
    <section className="ab-intro">
      <span className="ab-pill">Our purpose</span>
      <h1>About PharmaFlow</h1>
      <p>PharmaFlow is a web-based pharmacy management concept focused on making product browsing, prescription handling, order management, and delivery updates easier to understand and access.</p>
    </section>

    <section className="ab-cards" aria-label="What we stand for">
      {values.map(v => <article className="ab-card" key={v.title}>
        <div className="ab-icon" aria-hidden="true">{v.icon}</div>
        <h2>{v.title}</h2>
        <p>{v.text}</p>
      </article>)}
    </section>

    <section className="ab-split">
      <div>
        <h2>Why we built it</h2>
        <p>Getting medicine should not mean phoning around for stock, waiting in a queue to hand over a paper script, or wondering where your order is. We wanted one place where you can see what is available, send in a prescription, and follow your order until it reaches you.</p>
        <p>Behind the screen is a full pharmacy workflow. Pharmacists review scripts, the pharmacy team packs orders, couriers deliver them and administrators keep stock and records accurate. Each person sees only what their job needs.</p>
        <div className="ab-actions">
          <button className="hp-btn hp-btn-primary hp-btn-lg" onClick={() => onBrowse?.()}>Browse products</button>
          <button className="hp-btn hp-btn-ghost hp-btn-lg" onClick={onRegister}>Create an account</button>
        </div>
      </div>
      <aside className="ab-details" aria-label="Pharmacy details">
        <h3>Pharmacy details</h3>
        <dl>
          <div><dt>Responsible pharmacist</dt><dd>Ph. Naidoo</dd></div>
          <div><dt>Registration</dt><dd>SAPC pharmacy no. 0000000</dd></div>
          <div><dt>Opening hours</dt><dd>Mon–Fri 8:00–19:00<br />Sat 8:00–14:00</dd></div>
          <div><dt>Location</dt><dd>12 Long Street, Cape Town</dd></div>
          <div><dt>Email</dt><dd>support@pharmaflow.co.za</dd></div>
        </dl>
      </aside>
    </section>

    <section className="ab-facts" aria-label="PharmaFlow at a glance">
      {facts.map(f => <div key={f.value}><strong>{f.value}</strong><span>{f.label}</span></div>)}
    </section>

    <section className="ab-block">
      <div className="ab-head"><h2>How an order moves through PharmaFlow</h2><p>Four people handle every order, and you can see where it is at each step.</p></div>
      <ol className="ab-journey">
        {journey.map((s, i) => <li key={s.title}>
          <span className="ab-step">{i + 1}</span>
          <small>{s.who}</small>
          <h3>{s.title}</h3>
          <p>{s.text}</p>
        </li>)}
      </ol>
    </section>

    <section className="ab-block ab-standards">
      <div className="ab-head"><h2>Our standards</h2><p>The rules the system enforces so that care comes first.</p></div>
      <ul>
        {standards.map(s => <li key={s}><span aria-hidden="true">✓</span>{s}</li>)}
      </ul>
    </section>

    <section className="ab-cta">
      <div>
        <h2>Questions about an order or a prescription?</h2>
        <p>Email support@pharmaflow.co.za and a member of the pharmacy team will reply during opening hours.</p>
      </div>
      <button className="hp-btn hp-btn-light hp-btn-lg" onClick={onRegister}>Create a free account</button>
    </section>
  </div>;
}
