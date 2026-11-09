import React, { useState } from 'react';
import '../styles/contact.css';

const topics = ['Order or delivery', 'Prescription question', 'Product or stock enquiry', 'My account', 'Something else'];

const faqs = [
  { q: 'How long does a prescription take to verify?', a: 'Pharmacists review scripts during opening hours. Most are checked the same day. If something is unclear, we will message you through your account.' },
  { q: 'Can someone else collect my order?', a: 'Yes. Ask them to bring the order number and their ID. Orders with scheduled medicines are handed over to the named patient or an authorised person.' },
  { q: 'What if an item I need is out of stock?', a: 'Contact us and the pharmacy team will suggest an equivalent product or tell you when stock is due.' },
  { q: 'How do I change my delivery address?', a: 'Update it before your order is packed by contacting us with your order number. After packing, the courier will call you.' }
];

export default function Contact() {
  const [form, setForm] = useState({ name: '', email: '', topic: topics[0], message: '' });
  const [sent, setSent] = useState(false);
  const [open, setOpen] = useState(0);
  const set = e => setForm({ ...form, [e.target.name]: e.target.value });
  const submit = e => { e.preventDefault(); setSent(true); };

  return <div className="ct">
    <section className="ct-intro">
      <span className="ab-pill">Get in touch</span>
      <h1>Contact us</h1>
      <p>Questions about an order, a prescription or a product? Send us a message and the pharmacy team will reply during opening hours.</p>
    </section>

    <div className="ct-grid">
      <form className="ct-form" onSubmit={submit}>
        <h2>Send a message</h2>
        <div className="ct-two">
          <div className="au-field"><label htmlFor="name">Full name</label><input id="name" name="name" value={form.name} onChange={set} placeholder="Your name" required /></div>
          <div className="au-field"><label htmlFor="email">Email address</label><input id="email" name="email" type="email" value={form.email} onChange={set} placeholder="you@example.com" required /></div>
        </div>
        <div className="au-field"><label htmlFor="topic">What is this about?</label>
          <select id="topic" name="topic" value={form.topic} onChange={set}>{topics.map(t => <option key={t}>{t}</option>)}</select>
        </div>
        <div className="au-field"><label htmlFor="message">Message</label><textarea id="message" name="message" rows="6" value={form.message} onChange={set} placeholder="Tell us how we can help. Please do not include card numbers." required /></div>
        {sent && <div className="au-message" role="status">Prototype only: messages are not sent yet. Thank you, {form.name || 'friend'}!</div>}
        <button className="hp-btn hp-btn-primary hp-btn-lg" type="submit">Send message</button>
      </form>

      <aside className="ct-side">
        <div className="ct-card"><span aria-hidden="true">✉</span><div><h3>Email</h3><p>support@pharmaflow.co.za</p><small>We reply within one working day.</small></div></div>
        <div className="ct-card"><span aria-hidden="true">☎</span><div><h3>Phone</h3><p>021 555 0142</p><small>Pharmacist on duty during opening hours.</small></div></div>
        <div className="ct-card"><span aria-hidden="true">⌖</span><div><h3>Visit the pharmacy</h3><p>12 Long Street, Cape Town, 8001</p><small>Collection orders are ready at the front counter.</small></div></div>
        <div className="ct-hours">
          <h3>Opening hours</h3>
          <dl>
            <div><dt>Monday to Friday</dt><dd>8:00 – 19:00</dd></div>
            <div><dt>Saturday</dt><dd>8:00 – 14:00</dd></div>
            <div><dt>Sunday and public holidays</dt><dd>Closed</dd></div>
          </dl>
        </div>
        <div className="ct-urgent"><b>Medical emergency?</b> Do not use this form. Call 10177 for an ambulance or go to your nearest emergency unit.</div>
      </aside>
    </div>

    <section className="ct-faq">
      <h2>Common questions</h2>
      <div>
        {faqs.map((f, i) => <div className={`hp-q ${open === i ? 'open' : ''}`} key={f.q}>
          <button aria-expanded={open === i} onClick={() => setOpen(open === i ? -1 : i)}>{f.q}<span aria-hidden="true">{open === i ? '−' : '+'}</span></button>
          {open === i && <p>{f.a}</p>}
        </div>)}
      </div>
    </section>
  </div>;
}
