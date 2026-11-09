import React, { useEffect, useState } from 'react';
import Layout from './components/Layout';
import Homepage from './pages/Homepage';
import About from './pages/About';
import Products from './pages/Products';
import Login from './pages/Login';
import Register from './pages/Register';
import Contact from './pages/Contact';
import AdminDashboard from './pages/AdminDashboard';
import Profile from './pages/Profile';
import Cart from './pages/Cart';
import Orders from './pages/Orders';
import PharmacistDashboard from './pages/PharmacistDashboard';
import CourierDashboard from './pages/CourierDashboard';
import MyPrescriptions from './pages/MyPrescriptions';
import { categories } from './data/categories';
import { useCart } from './context/CartContext';
import { clearSession } from './services/api';
import './styles/theme.css';

function getStoredUser() {
  try {
    const saved =
      localStorage.getItem('pharmaflow_user') ||
      sessionStorage.getItem('pharmaflow_user');

    return saved ? JSON.parse(saved) : null;
  } catch {
    return null;
  }
}

export default function App() {
  const [page, setPage] = useState('home');
  const [user, setUser] = useState(getStoredUser);
  const [filter, setFilter] = useState({
    search: '',
    category: 'All'
  });
  const [toast, setToast] = useState('');

  const { itemCount } = useCart();

  const say = text => {
    setToast(text);
    window.clearTimeout(say.timer);
    say.timer = window.setTimeout(() => setToast(''), 2800);
  };

  const go = name => {
    setPage(name);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const requireLogin = message => {
    if (!getStoredUser()) {
      say(message);
      go('login');
      return false;
    }

    return true;
  };

  const openProfile = () => {
    if (requireLogin('Please sign in to view your profile.')) {
      go('profile');
    }
  };

  const openPrescriptions = () => {
    if (requireLogin('Please sign in to view your prescriptions.')) {
      go('prescriptions');
    }
  };

  const openOrders = () => {
    if (requireLogin('Please sign in to view your orders.')) {
      go('orders');
    }
  };

  const openCart = () => {
    go('cart');
  };

  const browse = term => {
    const isCategory = categories.some(
      category => category.name === term
    );

    setFilter({
      search: !term || isCategory ? '' : term,
      category: isCategory ? term : 'All'
    });

    go('products');
  };

  useEffect(() => {
    const handleLogin = event => {
      const loggedInUser = event.detail;

      if (!loggedInUser) return;

      setUser(loggedInUser);

      const role = String(
        loggedInUser.role || loggedInUser.userRole || ''
      ).trim().toUpperCase();

      if (role === 'PHARMACIST') {
        go('pharmacist');
        say(`Welcome back, ${loggedInUser.fullName || 'Pharmacist'}!`);
      } else if (role === 'ADMIN' || role === 'ADMINISTRATOR') {
        go('admin');
        say(`Welcome back, ${loggedInUser.fullName || 'Administrator'}!`);
      } else if (role === 'COURIER') {
        go('courier');
        say(`Welcome back, ${loggedInUser.fullName || 'Courier'}!`);
      } else {
        go('home');
        say(`Welcome back, ${loggedInUser.fullName || 'you'}!`);
      }
    };

    const requireCustomerLogin = () => {
      say('Please sign in to continue.');
      go('login');
    };

    const handleUserUpdated = event => {
      if (event.detail) setUser(event.detail);
    };

    const handleSessionExpired = () => {
      clearSession();
      window.dispatchEvent(new Event('pharmaflow-logout'));
      setUser(null);
      say('Your session expired. Please sign in again.');
      go('login');
    };

    const handleLogout = () => {
      localStorage.removeItem('pharmaflow_token');
      localStorage.removeItem('pharmaflow_user');
      sessionStorage.removeItem('pharmaflow_token');
      sessionStorage.removeItem('pharmaflow_user');

      setUser(null);
      go('home');
    };

    window.addEventListener('pharmaflow-login', handleLogin);
    window.addEventListener('pharmaflow-session-expired', handleSessionExpired);
    window.addEventListener('pharmaflow-user-updated', handleUserUpdated);
    window.addEventListener('pharmaflow-open-profile', openProfile);
    window.addEventListener('pharmaflow-open-cart', openCart);
    window.addEventListener(
      'pharmaflow-require-customer-login',
      requireCustomerLogin
    );
    window.addEventListener('pharmaflow-open-orders', openOrders);
    window.addEventListener(
      'pharmaflow-open-prescriptions',
      openPrescriptions
    );
    window.addEventListener('pharmaflow-logout', handleLogout);

    return () => {
      window.removeEventListener('pharmaflow-login', handleLogin);
      window.removeEventListener('pharmaflow-session-expired', handleSessionExpired);
      window.removeEventListener('pharmaflow-user-updated', handleUserUpdated);
      window.removeEventListener('pharmaflow-open-profile', openProfile);
      window.removeEventListener('pharmaflow-open-cart', openCart);
      window.removeEventListener(
        'pharmaflow-require-customer-login',
        requireCustomerLogin
      );
      window.removeEventListener('pharmaflow-open-orders', openOrders);
      window.removeEventListener(
        'pharmaflow-open-prescriptions',
        openPrescriptions
      );
      window.removeEventListener('pharmaflow-logout', handleLogout);

      window.clearTimeout(say.timer);
    };
  }, []);

  const logout = () => {
    localStorage.removeItem('pharmaflow_token');
    localStorage.removeItem('pharmaflow_user');
    sessionStorage.removeItem('pharmaflow_token');
    sessionStorage.removeItem('pharmaflow_user');

    window.dispatchEvent(new Event('pharmaflow-logout'));

    setUser(null);
    go('home');
    say('You have been logged out.');
  };

  const nav = {
    current: page,
    user,
    cartCount: itemCount,
    onCart: openCart,
    onLogout: logout,
    onHome: () => go('home'),
    onAbout: () => go('about'),
    onContact: () => go('contact'),
    onLogin: () => go('login'),
    onRegister: () => go('register'),
    onBrowse: browse,
    onNotify: say,
    onPharmacist: () => go('pharmacist')
  };

  const pages = {
    home: <Homepage {...nav} />,
    about: <About {...nav} />,
    products: (
      <Products
        key={`${filter.category}|${filter.search}`}
        initialSearch={filter.search}
        initialCategory={filter.category}
        onNotify={say}
      />
    ),
    cart: (
      <Cart
        onContinueShopping={() => go('products')}
        onCheckout={() => {
          if (requireLogin('Please sign in before checkout.')) go('cart');
        }}
      />
    ),
    prescriptions: <MyPrescriptions onNotify={say} />,
    login: <Login {...nav} />,
    register: <Register {...nav} />,
    contact: <Contact {...nav} />,
    profile: (
      <Profile
        user={user}
        onHome={() => go('home')}
        onNotify={say}
      />
    ),
    orders: (
      <Orders
        user={user}
        onBrowse={() => go('products')}
        onNotify={say}
      />
    ),
    admin: <AdminDashboard onExit={logout} />,
    courier: <CourierDashboard onExit={logout} />,
    pharmacist: (
      <PharmacistDashboard onHome={logout} onNotify={say} />
    )
  };

  return (
    <>
      {page === 'admin' ? (
        pages.admin
      ) : page === 'pharmacist' ? (
        pages.pharmacist
      ) : page === 'courier' ? (
        pages.courier
      ) : (
        <Layout {...nav}>
          {pages[page] || pages.home}
        </Layout>
      )}

      {toast && (
        <div
          role="status"
          style={{
            position: 'fixed',
            left: '50%',
            bottom: 24,
            transform: 'translateX(-50%)',
            background: '#0b2f63',
            color: '#fff',
            padding: '12px 20px',
            borderRadius: 999,
            fontFamily: "'Figtree', system-ui, sans-serif",
            fontSize: 14,
            zIndex: 100,
            boxShadow: '0 8px 24px #0004'
          }}
        >
          {toast}
        </div>
      )}
    </>
  );
}
