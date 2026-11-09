import React, { createContext, useContext, useEffect, useState } from 'react';

const CartContext = createContext(null);
const CART_KEY_PREFIX = 'pharmaflow_cart_';
const LEGACY_CART_KEY = 'pharmaflow_cart';

// Each account (and the logged-out "guest") gets its own cart.
function currentUserId() {
  try {
    const saved =
      localStorage.getItem('pharmaflow_user') ||
      sessionStorage.getItem('pharmaflow_user');
    const user = saved ? JSON.parse(saved) : null;

    return user?.id !== undefined && user?.id !== null ? String(user.id) : 'guest';
  } catch {
    return 'guest';
  }
}

function readCart(userId) {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY_PREFIX + userId)) || [];
  } catch {
    return [];
  }
}

export function CartProvider({ children }) {
  // userId and items live in one state object so they always change together.
  const [store, setStore] = useState(() => {
    // The old single shared cart is what leaked between accounts; drop it.
    localStorage.removeItem(LEGACY_CART_KEY);

    const userId = currentUserId();
    return { userId, items: readCart(userId) };
  });

  const cart = store.items;

  const setCart = update =>
    setStore(current => ({
      ...current,
      items: typeof update === 'function' ? update(current.items) : update
    }));

  useEffect(() => {
    const switchCart = () => {
      const userId = currentUserId();
      setStore({ userId, items: readCart(userId) });
    };

    window.addEventListener('pharmaflow-login', switchCart);
    window.addEventListener('pharmaflow-logout', switchCart);

    return () => {
      window.removeEventListener('pharmaflow-login', switchCart);
      window.removeEventListener('pharmaflow-logout', switchCart);
    };
  }, []);

  useEffect(() => {
    localStorage.setItem(CART_KEY_PREFIX + store.userId, JSON.stringify(store.items));
  }, [store]);

  function addToCart(product, quantity = 1) {
    setCart(current => {
      const existing = current.find(item => item.id === product.id);

      if (existing) {
        return current.map(item =>
          item.id === product.id
            ? {
                ...item,
                quantity: Math.min(item.quantity + quantity, Number.isFinite(Number(product.stock)) ? Math.max(0, Number(product.stock)) : item.quantity + quantity)
              }
            : item
        );
      }

      return [
        ...current,
        {
          ...product,
          quantity: Math.min(quantity, Number.isFinite(Number(product.stock)) ? Math.max(0, Number(product.stock)) : quantity)
        }
      ];
    });
  }

  function updateQuantity(productId, quantity) {
    setCart(current =>
      current
        .map(item =>
          item.id === productId
            ? { ...item, quantity: Math.min(Number.isFinite(Number(item.stock)) ? Math.max(0, Number(item.stock)) : Math.max(1, quantity), Math.max(1, Number(quantity) || 1)) }
            : item
        )
    );
  }

  function removeFromCart(productId) {
    setCart(current => current.filter(item => item.id !== productId));
  }

  function clearCart() {
    setCart([]);
  }

  const itemCount = cart.reduce((total, item) => total + item.quantity, 0);
  const total = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  return (
    <CartContext.Provider
      value={{
        cart,
        itemCount,
        total,
        addToCart,
        updateQuantity,
        removeFromCart,
        clearCart
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);

  if (!context) {
    throw new Error('useCart must be used inside CartProvider');
  }

  return context;
}