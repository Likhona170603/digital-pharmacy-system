import { useCallback, useEffect, useState } from 'react';
import api from '../services/api';

// The backend has no per-product reorder level, so one shared threshold is used.
export const LOW_STOCK_THRESHOLD = 10;

function toItem(p) {
  return {
    id: p.productId,
    name: p.productName,
    description: p.description || '',
    sku: p.sku || '',
    price: Number(p.price) || 0,
    stock: Number(p.stockQuantity) || 0,
    rx: Boolean(p.rxRequired),
    active: p.active !== false,
    categoryId: p.category?.categoryId || '',
    category: p.category?.categoryName || 'Uncategorised',
    schedule: p.category?.scheduleClassification || '—',
    form: p.category?.dosageForm || '',
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
      // /product/search returns inactive products too, so it needs a staff login.
      const [page, cats] = await Promise.all([
        api.get('/product/search?size=500'),
        api.get('/category/getAll')
      ]);

      const records = Array.isArray(page) ? page : page?.content ?? [];

      setInventory(records.map(toItem));
      setCategories(Array.isArray(cats) ? cats : []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  return { inventory, categories, loading, error, reload };
}
