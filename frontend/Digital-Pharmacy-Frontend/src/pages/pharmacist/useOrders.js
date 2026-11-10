import { useCallback, useEffect, useState } from 'react';
import api from '../../services/api';
import { formatDate } from './pharmacistUtils';

export default function useOrders({ onNotify } = {}) {
  const [orders, setOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [ordersError, setOrdersError] = useState('');
  const [busyOrder, setBusyOrder] = useState(null);
  const notify = text => onNotify?.(text);

  const loadOrders = useCallback(async () => {
    setOrdersLoading(true);
    setOrdersError('');

    try {
      const data = await api.get('/order/getall');
      const records = Array.isArray(data) ? data : [];

      let usersById = {};

      try {
        const users = await api.get('/user/names');

        usersById = Object.fromEntries(
          (Array.isArray(users) ? users : []).map(user => [
            String(user.id),
            user
          ])
        );
      } catch {
        usersById = {};
      }

      setOrders(
        records.map(order => ({
          ...order,
          id: order.orderNumber ?? order.id,
          customer:
            usersById[String(order.customerId)]?.fullName ||
            `Customer #${order.customerId ?? '?'}`,
          customerEmail:
            usersById[String(order.customerId)]?.email || '',
          orderItems: Array.isArray(order.items) ? order.items : [],
          items: Array.isArray(order.items)
            ? order.items.reduce(
                (sum, item) => sum + Number(item.orderQty || 0),
                0
              )
            : 0,
          placed: order.createdAt
            ? formatDate(order.createdAt)
            : '—',
          status: order.orderStatus || 'UNKNOWN'
        }))
      );
    } catch (err) {
      setOrdersError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to view all orders. Check that you are logged in with a pharmacist account.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not load orders.'
      );
    } finally {
      setOrdersLoading(false);
    }
  }, []);

  const packOrder = async order => {
    const id = order.orderNumber ?? order.id;

    if (!id) return;

    setBusyOrder(id);
    setOrdersError('');

    try {
      await api.post(`/order/${encodeURIComponent(String(id))}/pack`);

      notify(`Order ${id} marked as packed.`);
      await loadOrders();
    } catch (err) {
      setOrdersError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to pack orders. Check the backend pharmacist permissions.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not update this order.'
      );
    } finally {
      setBusyOrder(null);
    }
  };

  const collectOrder = async order => {
    const id = order.orderNumber ?? order.id;

    if (!id || !window.confirm(`Mark order ${id} as collected?`)) return;

    setBusyOrder(id);
    setOrdersError('');

    try {
      await api.post(`/order/${encodeURIComponent(String(id))}/collect`);

      notify(`Order ${id} marked as collected.`);
      await loadOrders();
    } catch (err) {
      setOrdersError(
        err.status === 401 || err.status === 403
          ? 'You are not authorised to mark orders as collected. Check the backend pharmacist permissions.'
          : err instanceof TypeError
            ? 'Cannot connect to the backend. Make sure Spring Boot is running.'
            : err.message || 'Could not update this order.'
      );
    } finally {
      setBusyOrder(null);
    }
  };

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  return {
    orders,
    ordersLoading,
    ordersError,
    loadOrders,
    busyOrder,
    packOrder,
    collectOrder
  };
}
