import React from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import '../styles/global.css';

// Wrap every page in this so the navbar and footer appear everywhere.
export default function Layout({ children, ...nav }) {
  return <div className="hp">
    <Navbar {...nav} />
    <main id="top">{children}</main>
    <Footer {...nav} />
  </div>;
}
