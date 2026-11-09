import React, { useEffect, useRef, useState } from 'react';
import '../styles/navbar.css';

export default function Navbar({
  onHome,
  onAbout,
  onContact,
  onLogin,
  onBrowse,
  onLogout,
  onCart,
  cartCount = 0,
  current,
  user
}) {
  const [query, setQuery] = useState('');
  const [profileOpen, setProfileOpen] = useState(false);
  const profileMenuRef = useRef(null);

  useEffect(() => {
    const handlePointerDown = event => {
      if (
        profileMenuRef.current &&
        !profileMenuRef.current.contains(event.target)
      ) {
        setProfileOpen(false);
      }
    };

    const handleKeyDown = event => {
      if (event.key === 'Escape') setProfileOpen(false);
    };

    document.addEventListener('pointerdown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('pointerdown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, []);

  const search = event => {
    event.preventDefault();
    onBrowse?.(query.trim());
  };

  const openFromMenu = eventName => {
    setProfileOpen(false);
    window.dispatchEvent(new CustomEvent(eventName));
  };

  const role = String(user?.role || user?.userRole || '').trim().toUpperCase();
  const isStaffAccount = ['ADMIN', 'ADMINISTRATOR', 'PHARMACIST', 'COURIER'].includes(role);
  const displayName =
    user?.fullName || user?.name || user?.email || 'My account';

  const initials = displayName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map(part => part[0].toUpperCase())
    .join('');

  return (
    <>
      <div className="hp-notice">
        <span className="hp-notice-icon" aria-hidden="true">✓</span>
        Free delivery on orders over R350
        <span className="hp-notice-divider">|</span>
        Registered pharmacist on duty Mon–Sat, 8:00–19:00
      </div>

      <header className="hp-header">
        <div className="hp-bar">
          <a
            className="hp-brand"
            href="/"
            onClick={event => {
              event.preventDefault();
              onHome?.();
            }}
            aria-label="PharmaFlow home"
          >
            <span className="hp-mark" aria-hidden="true">✚</span>
            <span>
              Pharma<b>Flow</b>
              <small>YOUR HEALTH, MADE SIMPLE</small>
            </span>
          </a>

          <form className="hp-search" onSubmit={search} role="search">
            <span className="hp-search-icon" aria-hidden="true">⌕</span>
            <input
              value={query}
              onChange={event => setQuery(event.target.value)}
              placeholder="Search medicines, vitamins and health products"
              aria-label="Search products"
            />
            {query && (
              <button
                className="hp-search-clear"
                type="button"
                onClick={() => setQuery('')}
                aria-label="Clear search"
              >
                ×
              </button>
            )}
            <button className="hp-search-submit" type="submit">
              Search
            </button>
          </form>

          <nav className="hp-actions" aria-label="Main navigation">
            {user && !isStaffAccount ? (
              <div ref={profileMenuRef} className="hp-profile">
                <button
                  type="button"
                  className="hp-profile-btn"
                  onClick={() => setProfileOpen(open => !open)}
                  aria-expanded={profileOpen}
                  aria-haspopup="true"
                >
                  <span className="hp-avatar" aria-hidden="true">{initials}</span>
                  <span>{displayName.split(/\s+/)[0]}</span>
                  <span aria-hidden="true">⌄</span>
                </button>

                {profileOpen && (
                  <div className="hp-profile-menu" role="menu">
                    <div className="hp-profile-who">
                      <strong>{displayName}</strong>
                      {user.email && <span>{user.email}</span>}
                    </div>
                    <button type="button" role="menuitem" onClick={() => openFromMenu('pharmaflow-open-profile')}>
                      My Profile
                    </button>
                    <button type="button" role="menuitem" onClick={() => openFromMenu('pharmaflow-open-orders')}>
                      My Orders
                    </button>
                    <button type="button" role="menuitem" onClick={() => openFromMenu('pharmaflow-open-prescriptions')}>
                      My Prescriptions
                    </button>
                    <button
                      type="button"
                      role="menuitem"
                      className="is-danger"
                      onClick={() => {
                        setProfileOpen(false);
                        onLogout?.();
                      }}
                    >
                      Log out
                    </button>
                  </div>
                )}
              </div>
            ) : !user ? (
              <button
                type="button"
                className={`hp-text-btn${current === 'login' ? ' is-active' : ''}`}
                onClick={onLogin}
              >
                Sign in
              </button>
            ) : null}

            <button
              type="button"
              className={`hp-text-btn${current === 'about' ? ' is-active' : ''}`}
              onClick={onAbout}
            >
              About us
            </button>

            <button
              type="button"
              className={`hp-text-btn${current === 'contact' ? ' is-active' : ''}`}
              onClick={onContact}
            >
              Contact
            </button>

            <button
              type="button"
              className={`hp-btn hp-cart-button${current === 'cart' ? ' is-active' : ''}`}
              onClick={onCart}
              aria-label={`Open shopping cart, ${cartCount} items`}
            >
              <span aria-hidden="true">🛒</span>
              <span>Cart</span>
              <span className="hp-cart-count">{cartCount}</span>
            </button>
          </nav>
        </div>
      </header>
    </>
  );
}
