const PHOTO_BY_ID = {
  P001: '/images/products/P001.webp',
  P002: '/images/products/P002.webp',
  P003: '/images/products/P003.webp',
  P004: '/images/products/P004.webp',
  P005: '/images/products/P005.webp',
  P006: '/images/products/P006.webp',
  P007: '/images/products/P007.webp',
  P008: '/images/products/P008.webp',
  P009: '/images/products/P009.webp',
  P010: '/images/products/P010.webp',
  P011: '/images/products/P011.webp',
  P012: '/images/products/P012.webp'
};

const unsplash = id => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=600&q=75`;

const POOLS = {
  pills: ['photo-1631549916768-4119b2e5f926', 'photo-1607619056574-7b8d3ee536b2', 'photo-1587854692152-cbe660dbde88'],
  bottles: ['photo-1758110197549-7e94845ad04b', 'photo-1576671081837-49000212a370'],
  blister: ['photo-1584308666744-24d5c474f2ae', 'photo-1628771065518-0d82f1938462']
};

const POOL_RULES = [
  [/syrup|liquid|suspension|drops|spray|nasal|cream|ointment|gel|lotion|inhaler|asthma/i, 'bottles'],
  [/chronic|prescription|rx|diabet|blood pressure|cholesterol|heart/i, 'blister']
];

export function photoFor(product = {}) {
  const id = String(product.id ?? product.productId ?? product.product_id ?? '');
  if (PHOTO_BY_ID[id]) return PHOTO_BY_ID[id];
  const text = `${product.name || product.productName || ''} ${product.category || ''} ${product.form || ''} ${product.schedule || ''}`;
  const rule = POOL_RULES.find(([pattern]) => pattern.test(text));
  const pool = POOLS[rule ? rule[1] : 'pills'];
  const hash = [...id].reduce((sum, char) => sum + char.charCodeAt(0), 0);
  return unsplash(pool[hash % pool.length]);
}
