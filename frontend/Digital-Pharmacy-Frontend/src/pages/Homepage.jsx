import React, { useState } from 'react';
import { categories } from '../data/categories';
import '../styles/homepage.css';

/* ---------- Icons (inline SVG, no external images) ---------- */
const ICONS = {
  pill: (<><path d="M10.5 20.5a5 5 0 0 1-7-7l9-9a5 5 0 0 1 7 7z" /><path d="m8.5 8.5 7 7" /></>),
  drop: <path d="M12 3s6 6.2 6 10.5A6 6 0 0 1 6 13.5C6 9.2 12 3 12 3z" />,
  sun: (<><circle cx="12" cy="12" r="4" /><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" /></>),
  pulse: <path d="M3 12h4l2-6 4 12 2-6h6" />,
  thermo: <path d="M14 14.8V5a2 2 0 0 0-4 0v9.8a4 4 0 1 0 4 0z" />,
  bandage: (<><rect x="1.5" y="8.5" width="21" height="7" rx="3.5" transform="rotate(-45 12 12)" /><path d="M10.5 10.5h.01M13.5 10.5h.01M10.5 13.5h.01M13.5 13.5h.01" /></>),
  heart: <path d="M12 20s-7.5-4.6-9-9.5A4.8 4.8 0 0 1 12 6a4.8 4.8 0 0 1 9 4.5C19.5 15.4 12 20 12 20z" />,
  leaf: (<><path d="M5 19c0-8 5-14 15-14 0 10-6 15-14 15" /><path d="M5 19c3-5 6-7 10-9" /></>),
  eye: (<><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12z" /><circle cx="12" cy="12" r="3" /></>),
  smile: (<><circle cx="12" cy="12" r="9" /><path d="M8 14s1.5 2 4 2 4-2 4-2M9 9.5h.01M15 9.5h.01" /></>),
  shield: (<><path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z" /><path d="m8.5 12 2.5 2.5 4.5-5" /></>),
  pharmacist: (<><circle cx="12" cy="8" r="4" /><path d="M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7" /><path d="M12 15v3M10.5 16.5h3" /></>),
  laptop: (<><rect x="4" y="5" width="16" height="11" rx="2" /><path d="M2 20h20" /></>),
  search: (<><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></>),
  file: (<><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z" /><path d="M14 3v5h5M9 13h6M9 17h4" /></>),
  arrow: <path d="M5 12h14M13 6l6 6-6 6" />,
  plus: <path d="M12 5v14M5 12h14" />,
  minus: <path d="M5 12h14" />,
  check: <path d="m5 12.5 4.5 4.5L19 7.5" />,
  cross: <path d="M9 3h6v6h6v6h-6v6H9v-6H3V9h6z" />
};

function Icon({ name }) {
  return (
    <svg
      className="ph-icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {ICONS[name]}
    </svg>
  );
}

const FALLBACK_ICONS = ['pill', 'sun', 'drop', 'heart', 'bandage', 'leaf'];

function pickIcon(name = '', index = 0) {
  const n = name.toLowerCase();
  if (/vitamin|supplement|nutri/.test(n)) return 'sun';
  if (/skin|beauty|cosmetic|derm|hair/.test(n)) return 'drop';
  if (/pain|relief|analges/.test(n)) return 'pulse';
  if (/cold|flu|cough|allerg|fever|sinus/.test(n)) return 'thermo';
  if (/first aid|wound|bandage|plaster/.test(n)) return 'bandage';
  if (/baby|child|kid|mother|mom/.test(n)) return 'smile';
  if (/eye|ear/.test(n)) return 'eye';
  if (/herbal|natural|organic/.test(n)) return 'leaf';
  if (/personal|care|hygiene|oral|dental/.test(n)) return 'heart';
  if (/prescription|medic|rx|tablet|capsule/.test(n)) return 'pill';
  return FALLBACK_ICONS[index % FALLBACK_ICONS.length];
}

const unsplash = id => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=900&q=80`;

const HERO_PHOTO_A = unsplash('photo-1763668177859-0ed5669a795e');
const HERO_PHOTO_B = unsplash('photo-1598662972299-5408ddb8a3dc');

const PHARMACIST_PHOTO = unsplash('photo-1580281657529-557a6abb6387');

const CATEGORY_PHOTOS = [
  { match: /pain/i, src: unsplash('photo-1471864190281-a93a3070b6de'), alt: 'Assorted tablets and capsules' },
  { match: /cough|cold|flu/i, src: unsplash('photo-1635166304271-04931640a450'), alt: 'Bottle of liquid medicine on a table' },
  { match: /allerg|hay ?fever/i, src: unsplash('photo-1758812928057-d06c9949a348'), alt: 'Man outdoors blowing his nose into a tissue' },
  { match: /vitamin|supplement/i, src: unsplash('photo-1644432757359-b184377d9eb0'), alt: 'Yellow vitamin D capsules in a glass bowl' },
  { match: /chronic|long.?term/i, src: unsplash('photo-1593086586351-1673fca190cf'), alt: 'Stethoscope beside a glass mug' },
  { match: /respir|asthma|breath/i, src: unsplash('photo-1645273474732-40e757681b97'), alt: 'Person holding an inhaler' }
];

const photoFor = (name = '') => CATEGORY_PHOTOS.find(p => p.match.test(name));

export default function Homepage({ onLogin, onRegister, onBrowse }) {
  const [activeFaq, setActiveFaq] = useState(0);

  const faqs = [
    {
      question: 'Can I order prescription medicine online?',
      answer: 'You can submit your prescription for pharmacist review. Prescription medicines must be approved by a pharmacist before they can be supplied.'
    },
    {
      question: 'Can I shop for non-prescription products?',
      answer: 'Yes. Browse the available catalogue for eligible over-the-counter products and health essentials.'
    },
    {
      question: 'How do I manage my orders?',
      answer: 'Sign in to your account to access the ordering features available to you.'
    }
  ];

  return (
    <main className="pharma-home">
      {/* ---------- Hero ---------- */}
      <section className="ph-hero">
        <div className="ph-container ph-hero-grid">
          <div className="ph-hero-copy">
            <h1>Everyday health, made simple.</h1>
            <p>
              Shop your health essentials and send prescriptions for pharmacist
              review, all from one convenient place.
            </p>

            <div className="ph-actions">
              <button className="ph-btn ph-btn-light" onClick={() => onBrowse?.()}>
                Shop products <Icon name="arrow" />
              </button>
              <button className="ph-btn ph-btn-ghost" onClick={onRegister}>
                Submit a prescription
              </button>
            </div>

            <ul className="ph-hero-points">
              <li><Icon name="check" /> Convenient online shopping</li>
              <li><Icon name="check" /> Pharmacist-led prescription review</li>
            </ul>
          </div>

          <div className="ph-hero-visual" aria-hidden="true">
            <div className="ph-orb ph-orb-a"><img src={HERO_PHOTO_A} alt="" /></div>
            <div className="ph-orb ph-orb-b"><img src={HERO_PHOTO_B} alt="" /></div>

            <div className="ph-rx-card">
              <div className="ph-rx-head">
                <span className="ph-rx-icon"><Icon name="file" /></span>
                <div>
                  <strong>Your prescription</strong>
                  <span>Sent for pharmacist review</span>
                </div>
              </div>
              <ol className="ph-rx-steps">
                <li className="is-done"><span><Icon name="check" /></span>Submitted</li>
                <li className="is-active"><span /> Pharmacist review</li>
                <li><span /> Approved</li>
              </ol>
            </div>
          </div>
        </div>
      </section>

      {/* ---------- Trust strip ---------- */}
      <div className="ph-container ph-trust-wrap">
        <div className="ph-trust">
          <div className="ph-trust-item">
            <span className="ph-trust-icon"><Icon name="shield" /></span>
            <div><strong>Health first</strong><span>Wellness for everyday life</span></div>
          </div>
          <div className="ph-trust-item">
            <span className="ph-trust-icon"><Icon name="pharmacist" /></span>
            <div><strong>Pharmacist support</strong><span>Prescription review by a pharmacist</span></div>
          </div>
          <div className="ph-trust-item">
            <span className="ph-trust-icon"><Icon name="laptop" /></span>
            <div><strong>Shop with convenience</strong><span>Browse from wherever you are</span></div>
          </div>
          <div className="ph-trust-item">
            <span className="ph-trust-icon"><Icon name="heart" /></span>
            <div><strong>Care made personal</strong><span>Your health journey matters</span></div>
          </div>
        </div>
      </div>

      {/* ---------- Categories ---------- */}
      <section className="ph-section">
        <div className="ph-container">
          <div className="ph-heading">
            <div>
              <h2>Shop by category</h2>
              <p>Find the essentials for your health and everyday routine.</p>
            </div>
            <button className="ph-link" onClick={() => onBrowse?.()}>
              View all products <Icon name="arrow" />
            </button>
          </div>

          <div className="ph-category-grid">
            {categories.slice(0, 6).map((category, index) => {
              const photo = photoFor(category.name);
              return (
                <button
                  key={category.name}
                  className={`ph-category tone-${index % 6}${photo ? ' has-photo' : ''}`}
                  onClick={() => onBrowse?.(category.name)}
                >
                  {photo && (
                    <>
                      <img className="ph-category-photo" src={photo.src} alt={photo.alt} loading="lazy" />
                      <span className="ph-category-shade" />
                    </>
                  )}
                  <span className="ph-category-icon"><Icon name={pickIcon(category.name, index)} /></span>
                  <span className="ph-category-name">{category.name}</span>
                  <span className="ph-category-arrow"><Icon name="arrow" /></span>
                </button>
              );
            })}
          </div>
        </div>
      </section>

      {/* ---------- Prescription ---------- */}
      <section className="ph-section">
        <div className="ph-container">
          <div className="ph-rx-band">
            <div className="ph-rx-band-copy">
              <h2>Prescription care, made simpler.</h2>
              <p>
                Submit your prescription through your account for pharmacist
                review. A more convenient way to manage your care.
              </p>
              <button className="ph-btn ph-btn-light" onClick={onRegister}>
                Get started <Icon name="arrow" />
              </button>
              <div className="ph-rx-note">
                <Icon name="cross" />
                <span>
                  Prescription medicines are subject to pharmacist review and
                  applicable requirements.
                </span>
              </div>
            </div>

            <div className="ph-rx-photo">
              <img
                src={PHARMACIST_PHOTO}
                alt="Pharmacist checking a medicine from the pharmacy shelf"
                loading="lazy"
              />
              <div className="ph-doc-stamp">
                <Icon name="shield" />
                <div>
                  <strong>Pharmacist review</strong>
                  <span>Required before supply</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ---------- How it works ---------- */}
      <section className="ph-section ph-section-tint">
        <div className="ph-container">
          <div className="ph-heading ph-heading-center">
            <div>
              <h2>Getting started is straightforward</h2>
              <p>Three steps with PharmaFlow.</p>
            </div>
          </div>

          <ol className="ph-steps">
            <li className="ph-step">
              <span className="ph-step-num">1</span>
              <span className="ph-step-icon"><Icon name="search" /></span>
              <h3>Explore</h3>
              <p>Browse the catalogue and find the products you need.</p>
            </li>
            <li className="ph-step">
              <span className="ph-step-num">2</span>
              <span className="ph-step-icon"><Icon name="file" /></span>
              <h3>Submit</h3>
              <p>Submit a prescription when one is required.</p>
            </li>
            <li className="ph-step">
              <span className="ph-step-num">3</span>
              <span className="ph-step-icon"><Icon name="heart" /></span>
              <h3>Take care</h3>
              <p>Use your account to access the available pharmacy services.</p>
            </li>
          </ol>
        </div>
      </section>

      {/* ---------- FAQ ---------- */}
      <section className="ph-section">
        <div className="ph-container ph-faq">
          <div className="ph-faq-intro">
            <h2>Questions, answered</h2>
            <p>Need a little more information before getting started?</p>
            <button className="ph-link" onClick={onLogin}>
              Visit your account <Icon name="arrow" />
            </button>
          </div>

          <div className="ph-faq-list">
            {faqs.map((faq, index) => {
              const open = activeFaq === index;
              return (
                <div className={`ph-faq-item ${open ? 'is-open' : ''}`} key={faq.question}>
                  <button
                    id={`faq-q-${index}`}
                    aria-expanded={open}
                    aria-controls={`faq-a-${index}`}
                    onClick={() => setActiveFaq(open ? -1 : index)}
                  >
                    <span>{faq.question}</span>
                    <span className="ph-faq-toggle"><Icon name={open ? 'minus' : 'plus'} /></span>
                  </button>
                  {open && (
                    <p id={`faq-a-${index}`} role="region" aria-labelledby={`faq-q-${index}`}>
                      {faq.answer}
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ---------- Closing call to action ---------- */}
      <section className="ph-cta-wrap">
        <div className="ph-container">
          <div className="ph-cta">
            <h2>Care that fits your day.</h2>
            <p>Create an account to shop, submit prescriptions and manage your orders.</p>
            <div className="ph-actions ph-actions-center">
              <button className="ph-btn ph-btn-light" onClick={onRegister}>
                Create an account <Icon name="arrow" />
              </button>
              <button className="ph-btn ph-btn-ghost" onClick={onLogin}>
                Sign in
              </button>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
