import { useCallback, useEffect, useState } from 'react';
import api from '../services/api';

export const LOW_STOCK_THRESHOLD = 10;

function toItem(p) {
  return {
    ...p,
    id: p.productId ?? p.id,
    name: p.productName ?? p.name ?? 'Unnamed product',
    description: p.description || '',
    sku: p.sku || '',
    price: Number(p.price) || 0,
    stock: Number(p.stockQuantity ?? p.stock) || 0,
    rx: Boolean(p.rxRequired ?? p.rx),
    active: p.active !== false,
    categoryId: p.category?.categoryId ?? p.categoryId ?? '',
    category: p.category?.categoryName ?? p.categoryName ?? p.category ?? 'Uncategorised',
    schedule: p.category?.scheduleClassification ?? p.scheduleClassification ?? '—',
    form: p.category?.dosageForm ?? p.dosageForm ?? '',
    reorder: LOW_STOCK_THRESHOLD
  };
}

export default function useInventory() {
  const [inventory, setInventory] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const reload = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [page, cats] = await Promise.all([
        api.get('/product/search?size=500'),
        api.get('/category/getAll')
      ]);
      const records = Array.isArray(page) ? page : page?.content || [];
      setInventory(records.map(toItem));
      setCategories(Array.isArray(cats) ? cats : []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { reload(); }, [reload]);
  return { inventory, categories, loading, error, reload };
}
